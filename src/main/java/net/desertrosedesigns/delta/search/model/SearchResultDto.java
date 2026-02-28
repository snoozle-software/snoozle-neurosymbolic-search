package net.desertrosedesigns.delta.search.model;

public class SearchResultDto {
    private final ProductDto product;
    private final double textScore;
    private final double vectorScore;
    private final double combinedScore;

    public SearchResultDto(ProductDto product, double textScore, double vectorScore, double combinedScore) {
        this.product = product;
        this.textScore = textScore;
        this.vectorScore = vectorScore;
        this.combinedScore = combinedScore;
    }

    public ProductDto getProduct() {
        return product;
    }

    public double getTextScore() {
        return textScore;
    }

    public double getVectorScore() {
        return vectorScore;
    }

    public double getCombinedScore() {
        return combinedScore;
    }
}
