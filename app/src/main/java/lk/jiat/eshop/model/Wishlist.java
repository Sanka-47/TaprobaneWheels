package lk.jiat.eshop.model;

import com.google.firebase.firestore.Exclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Wishlist {
//    @Getter(onMethod_ = {@Exclude})
//    @Setter(onMethod_ = {@Exclude})
    private String documentId;
    private String productId;

    public Wishlist(String productId) {
        this.productId = productId;
    }
}
