package net.desertrosedesigns.delta.search.ingest;

import net.desertrosedesigns.delta.search.service.EmbeddingService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProductCsvImporter {

    private final Driver driver;
    private final EmbeddingService embeddingService;
    private final Path sourceFile;

    public ProductCsvImporter(Driver driver,
                              EmbeddingService embeddingService,
                              @Value("${product.csv.path:/data/products.csv}") String csvPath) {
        this.driver = driver;
        this.embeddingService = embeddingService;
        this.sourceFile = Path.of(csvPath);
    }

    public int importProducts() throws IOException {
        try (Session session = driver.session()) {
            ensureIndexes(session);
            try (CSVParser parser = CSVParser.parse(sourceFile.toFile(), StandardCharsets.UTF_8,
                    CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                int imported = 0;
                for (var record : parser) {
                    ingestRecord(session, record.toMap());
                    imported++;
                }
                return imported;
            }
        }
    }

    private void ensureIndexes(Session session) {
        session.writeTransaction(tx -> {
            tx.run(
                    "CREATE FULLTEXT INDEX productTextIndex IF NOT EXISTS " +
                            "FOR (p:Product) " +
                            "ON EACH [p.title, p.description, p.item_details, p.categories, p.text]"
            ).consume();
            return null;
        });
    }

    private void ingestRecord(Session session, Map<String, String> record) {
        String title = record.getOrDefault("title", "");
        String description = record.getOrDefault("description", "");
        String itemDetails = record.getOrDefault("item_details", "");
        List<String> categories = parseCategories(record.getOrDefault("categories", ""));
        String combinedText = Stream.of(title, description, itemDetails, String.join(" ", categories))
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
        List<Double> embedding = embeddingService.embed(combinedText);
        Map<String, Object> params = new HashMap<>();
        params.put("sourceId", UUID.randomUUID().toString());
        params.put("title", title);
        params.put("description", description);
        params.put("itemDetails", itemDetails);
        params.put("text", combinedText);
        params.put("categories", categories);
        params.put("embedding", embedding);

        session.writeTransaction((TransactionWork<Void>) tx -> {
            tx.run("MERGE (p:Product {sourceId: $sourceId}) " +
                    "SET p.title = $title, p.description = $description, " +
                    "p.item_details = $itemDetails, p.text = $text, p.categories = $categories, " +
                    "p.embedding = $embedding",
                    params);
            return null;
        });
    }

    private List<String> parseCategories(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String cleaned = raw.replace("[", "").replace("]", "");
        return Stream.of(cleaned.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
