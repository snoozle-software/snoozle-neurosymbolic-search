package net.desertrosedesigns.delta.search.util;

import java.util.List;

public final class VectorUtils {
    private VectorUtils() {
    }

    public static double dotProduct(List<Double> a, List<Double> b) {
        double sum = 0;
        for (int i = 0; i < Math.min(a.size(), b.size()); i++) {
            sum += a.get(i) * b.get(i);
        }
        return sum;
    }

    public static double norm(List<Double> vector) {
        return Math.sqrt(vector.stream().mapToDouble(v -> v * v).sum());
    }

    public static double cosineSimilarity(List<Double> a, List<Double> b) {
        double denom = norm(a) * norm(b);
        if (denom == 0) {
            return 0d;
        }
        return dotProduct(a, b) / denom;
    }
}
