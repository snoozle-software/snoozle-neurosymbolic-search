package net.desertrosedesigns.delta.search.util;

public final class HybridScorer {
    private static final double TEXT_WEIGHT = 0.4;
    private static final double VECTOR_WEIGHT = 0.6;

    private HybridScorer() {
    }

    public static double compute(double textScore, double vectorScore) {
        return textScore * TEXT_WEIGHT + vectorScore * VECTOR_WEIGHT;
    }
}
