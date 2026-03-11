package lk.jiat.eshop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String uid;
    private String name;
    private String email;
    private String profilePicUrl;
    private String contact;
    private String address1;
    private String address2;
    private String postalCode;
    private String city;
}
