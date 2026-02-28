package net.desertrosedesigns.delta.search.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HybridScorerTest {

    @Test
    void computeAppliesWeights() {
        double textScore = 0.5;
        double vectorScore = 0.8;
        double combined = HybridScorer.compute(textScore, vectorScore);
        assertThat(combined).isEqualTo(0.5 * 0.4 + 0.8 * 0.6);
    }
}
