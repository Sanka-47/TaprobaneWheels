package lk.jiat.eshop.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private String productId;
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private List<String> images;
    private int stockCount;
    private boolean status;
    private float rating;
    private List<Attribute> attributes;

    public Product(String productId, String title, String description, double price, String categoryId, List<String> images, int stockCount, boolean status) {
        this.productId = productId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.images = images;
        this.stockCount = stockCount;
        this.status = status;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {
        private String name;
        private String type;
        private List<String> values;
    }
}
