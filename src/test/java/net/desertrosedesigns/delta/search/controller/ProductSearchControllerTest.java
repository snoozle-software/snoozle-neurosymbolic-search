package net.desertrosedesigns.delta.search.controller;

import net.desertrosedesigns.delta.search.ingest.ProductCsvImporter;
import net.desertrosedesigns.delta.search.model.SearchResultDto;
import net.desertrosedesigns.delta.search.service.ProductSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchControllerTest {

    @Mock
    private ProductCsvImporter importer;

    @Mock
    private ProductSearchService searchService;

    @InjectMocks
    private ProductSearchController controller;

    @Test
    void importProductsReportsCount() throws IOException {
        doReturn(5).when(importer).importProducts();
        ResponseEntity<Map<String, Object>> response = controller.importProducts();
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsEntry("imported", 5);
    }

    @Test
    void textSearchDelegatesToService() {
        SearchResultDto dto = new SearchResultDto(null, 1.0, 0.0, 1.0);
        when(searchService.textSearch("q", 20)).thenReturn(List.of(dto));
        List<SearchResultDto> results = controller.textSearch("q", 20);
        assertThat(results).containsExactly(dto);
    }

    @Test
    void embeddingSearchDelegatesToService() {
        SearchResultDto dto = new SearchResultDto(null, 0.0, 1.0, 1.0);
        when(searchService.embeddingSearch("q", 10)).thenReturn(List.of(dto));
        List<SearchResultDto> results = controller.embeddingSearch("q", 10);
        assertThat(results).containsExactly(dto);
    }

    @Test
    void hybridSearchDelegatesToService() {
        SearchResultDto dto = new SearchResultDto(null, 0.4, 0.6, 0.56);
        when(searchService.hybridSearch("q", 5)).thenReturn(List.of(dto));
        List<SearchResultDto> results = controller.hybridSearch("q", 5);
        assertThat(results).containsExactly(dto);
    }
}
