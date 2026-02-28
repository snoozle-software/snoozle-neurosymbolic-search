package net.desertrosedesigns.delta.search.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProductDto {
    private final String sourceId;
    private final String title;
    private final String description;
    private final String itemDetails;
    private final List<String> categories;
    private final String text;

    public ProductDto(String sourceId, String title, String description, String itemDetails, List<String> categories, String text) {
        this.sourceId = sourceId;
        this.title = title;
        this.description = description;
        this.itemDetails = itemDetails;
        this.categories = categories;
        this.text = text;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getItemDetails() {
        return itemDetails;
    }

    public List<String> getCategories() {
        return categories;
    }

    public String getText() {
        return text;
    }

    public static ProductDto fromNode(Map<String, Object> properties) {
        return new ProductDto(
                (String) properties.getOrDefault("sourceId", ""),
                (String) properties.getOrDefault("title", ""),
                (String) properties.getOrDefault("description", ""),
                (String) properties.getOrDefault("item_details", ""),
                parseCategories(properties.get("categories")),
                (String) properties.getOrDefault("text", "")
        );
    }

    @SuppressWarnings("unchecked")
    private static java.util.List<String> parseCategories(Object raw) {
        if (raw instanceof java.util.List<?>) {
            return (java.util.List<String>) raw;
        }
        return List.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDto that = (ProductDto) o;
        return Objects.equals(sourceId, that.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId);
    }
}
