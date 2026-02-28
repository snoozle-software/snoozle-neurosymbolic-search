package net.desertrosedesigns.delta.search.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VectorUtilsTest {

    @Test
    void cosineSimilarityIsOneForSameVector() {
        List<Double> vector = List.of(1.0, 2.0, 3.0);
        double similarity = VectorUtils.cosineSimilarity(vector, vector);
        assertThat(similarity).isEqualTo(1.0);
    }

    @Test
    void cosineSimilarityIsZeroForOrthogonalVectors() {
        List<Double> a = List.of(1.0, 0.0);
        List<Double> b = List.of(0.0, 1.0);
        assertThat(VectorUtils.cosineSimilarity(a, b)).isEqualTo(0.0);
    }
}
