package net.desertrosedesigns.delta.search.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SentenceTransformerEmbeddingService implements EmbeddingService {

    private static final int MAX_RETRIES = 5;
    private static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    private final WebClient webClient;
    private final String serviceUrl;
    private final Map<String, List<Double>> cache = new ConcurrentHashMap<>();

    public SentenceTransformerEmbeddingService(WebClient webClient,
                                               @Value("${embedding.service-url}") String serviceUrl) {
        this.webClient = webClient;
        this.serviceUrl = serviceUrl;
    }

    @Override
    public List<Double> embed(String text) {
        return cache.computeIfAbsent(text, this::callService);
    }

    private List<Double> callService(String text) {
        EmbedRequest request = new EmbedRequest(List.of(text));
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                EmbedResponse response = webClient.post()
                        .uri(serviceUrl)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(EmbedResponse.class)
                        .timeout(Duration.ofSeconds(30))
                        .block();
                if (response == null || response.embeddings().isEmpty()) {
                    return Collections.emptyList();
                }
                return response.embeddings().get(0);
            } catch (Throwable t) {
                if (attempts >= MAX_RETRIES) {
                    throw t;
                }
                try {
                    Thread.sleep(RETRY_DELAY.toMillis());
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while backing off to the embedding service", interrupted);
                }
            }
        }
    }

    private record EmbedRequest(List<String> inputs) {}

    private record EmbedResponse(List<List<Double>> embeddings) {}
}
