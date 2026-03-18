package lk.jiat.eshop.model;

import com.google.firebase.firestore.Exclude;

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
    @Exclude
    private String id;
    private String productId;
    private String brand;
    private String model;
    private String title;
    private String description;
    private double price;
    private String categoryId;
    private List<String> images;
    private int stockCount;
    private boolean status;
    private float rating;
    private List<Attribute> attributes;

    public Product(String productId, String brand, String model, String description, double price, String categoryId, List<String> images, int stockCount, boolean status) {
        this.productId = productId;
        this.brand = brand;
        this.model = model;
        this.title = brand + " " + model;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
        this.images = images;
        this.stockCount = stockCount;
        this.status = status;
    }

    public void setTitleFromBrandModel() {
        this.title = this.brand + " " + this.model;
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
