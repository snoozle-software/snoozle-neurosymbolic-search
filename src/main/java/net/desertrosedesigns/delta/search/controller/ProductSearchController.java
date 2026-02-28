package net.desertrosedesigns.delta.search.controller;

import net.desertrosedesigns.delta.search.ingest.ProductCsvImporter;
import net.desertrosedesigns.delta.search.model.SearchResultDto;
import net.desertrosedesigns.delta.search.service.ProductSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductSearchController {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchController.class);

    private final ProductCsvImporter importer;
    private final ProductSearchService searchService;

    public ProductSearchController(ProductCsvImporter importer, ProductSearchService searchService) {
        this.importer = importer;
        this.searchService = searchService;
    }

    @GetMapping("/products/import")
    public ResponseEntity<Map<String, Object>> importProducts() {
        try {
            int count = importer.importProducts();
            return ResponseEntity.ok(Map.of("imported", count));
        } catch (IOException e) {
            log.error("product import failed", e);
            return ResponseEntity.status(500).body(Map.of("error", "import failed", "reason", e.getMessage()));
        }
    }

    @GetMapping("/search/text")
    public List<SearchResultDto> textSearch(@RequestParam("q") String query,
                                            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return searchService.textSearch(query, limit);
    }

    @GetMapping("/search/embedding")
    public List<SearchResultDto> embeddingSearch(@RequestParam("q") String query,
                                                @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return searchService.embeddingSearch(query, limit);
    }

    @GetMapping("/search/hybrid")
    public List<SearchResultDto> hybridSearch(@RequestParam("q") String query,
                                              @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return searchService.hybridSearch(query, limit);
    }
}
