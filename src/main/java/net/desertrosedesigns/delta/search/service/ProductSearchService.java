package net.desertrosedesigns.delta.search.service;

import net.desertrosedesigns.delta.search.model.ProductDto;
import net.desertrosedesigns.delta.search.model.SearchResultDto;
import net.desertrosedesigns.delta.search.util.VectorUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductSearchService {

    private final Driver driver;
    private final EmbeddingService embeddingService;

    public ProductSearchService(Driver driver, EmbeddingService embeddingService) {
        this.driver = driver;
        this.embeddingService = embeddingService;
    }

    public List<SearchResultDto> textSearch(String query, int limit) {
        try (Session session = driver.session()) {
            return session.readTransaction((TransactionWork<List<SearchResultDto>>) tx -> tx.run(
                            "CALL db.index.fulltext.queryNodes('productTextIndex', $query) " +
                                    "YIELD node, score RETURN node, score ORDER BY score DESC LIMIT $limit",
                            Map.of("query", query, "limit", limit))
                    .list(record -> buildTextResult(record.get("node").asNode(), record.get("score").asDouble())));
        }
    }

    public List<SearchResultDto> embeddingSearch(String query, int limit) {
        List<Double> queryEmbedding = embeddingService.embed(query);
        if (queryEmbedding.isEmpty()) {
            return List.of();
        }
        try (Session session = driver.session()) {
            return session.readTransaction((TransactionWork<List<SearchResultDto>>) tx -> tx.run(
                            "MATCH (p:Product) WHERE p.embedding IS NOT NULL RETURN p",
                            Map.of())
                    .list(record -> buildVectorResult(record.get("p").asNode(), queryEmbedding)))
                    .stream()
                    .sorted(Comparator.comparingDouble(SearchResultDto::getVectorScore).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }

    public List<SearchResultDto> hybridSearch(String query, int limit) {
        List<Double> queryEmbedding = embeddingService.embed(query);
        if (queryEmbedding.isEmpty()) {
            return textSearch(query, limit);
        }
        try (Session session = driver.session()) {
            return session.readTransaction((TransactionWork<List<SearchResultDto>>) tx -> tx.run(
                            "CALL db.index.fulltext.queryNodes('productTextIndex', $query) " +
                                    "YIELD node, score RETURN node, score ORDER BY score DESC LIMIT $limit",
                            Map.of("query", query, "limit", limit))
                    .list(record -> buildHybridResult(record.get("node").asNode(), record.get("score").asDouble(), queryEmbedding)));
        }
    }

    private SearchResultDto buildTextResult(Node node, double textScore) {
        ProductDto product = ProductDto.fromNode(node.asMap());
        return new SearchResultDto(product, textScore, 0, textScore);
    }

    private SearchResultDto buildVectorResult(Node node, List<Double> queryEmbedding) {
        ProductDto product = ProductDto.fromNode(node.asMap());
        double vectorScore = VectorUtils.cosineSimilarity(nodeEmbedding(node), queryEmbedding);
        return new SearchResultDto(product, 0, vectorScore, vectorScore);
    }

    private SearchResultDto buildHybridResult(Node node, double textScore, List<Double> queryEmbedding) {
        ProductDto product = ProductDto.fromNode(node.asMap());
        double vectorScore = VectorUtils.cosineSimilarity(nodeEmbedding(node), queryEmbedding);
        double combined = textScore * 0.4 + vectorScore * 0.6;
        return new SearchResultDto(product, textScore, vectorScore, combined);
    }

    private List<Double> nodeEmbedding(Node node) {
        Value value = node.get("embedding");
        if (value == null || value.isNull()) {
            return List.of();
        }
        return value.asList(v -> v.asDouble());
    }
}
