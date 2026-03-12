package lk.jiat.eshop.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.jiat.eshop.R;
import lk.jiat.eshop.activity.MainActivity;
import lk.jiat.eshop.activity.SignInActivity;
import lk.jiat.eshop.adapter.ProductSliderAdapter;
import lk.jiat.eshop.adapter.SectionAdapter;
import lk.jiat.eshop.databinding.FragmentProductDetailsBinding;
import lk.jiat.eshop.model.CartItem;
import lk.jiat.eshop.model.Product;
import lk.jiat.eshop.model.Wishlist;
import lk.jiat.eshop.util.ShakeDetector;


public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private String productId;
    private int quantity = 1;
    private int avbQuantity;
    private boolean isInWishlist = false;
    private String wishlistDocId;
    private Product currentProduct;

    private Map<String, ChipGroup> attributeGroups = new HashMap<>();

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }

        mSensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(count -> {
            shareProduct();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().findViewById(R.id.bottom_navigation_view).setVisibility(View.GONE);

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });


        // Load Product Details
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").whereEqualTo("productId", productId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    currentProduct = qds.getDocuments().get(0).toObject(Product.class);

                    ProductSliderAdapter adapter = new ProductSliderAdapter(currentProduct.getImages());
                    binding.productImageSlider.setAdapter(adapter);

                    binding.dotsIndicator.attachTo(binding.productImageSlider);

                    binding.productDetailsTitle.setText(currentProduct.getTitle());

                    binding.productDetailsRating.setRating(currentProduct.getRating());

                    binding.productDetailsPrice.setText("LKR " + currentProduct.getPrice());

                    binding.productDetailsAvbQty.setText(String.valueOf(currentProduct.getStockCount()));
                    avbQuantity = currentProduct.getStockCount();

                    if (currentProduct.getAttributes() != null) {

                        currentProduct.getAttributes().forEach(attribute -> {
                            renderAttribute(attribute, binding.productDetailsAttributeContainer);

                        });

                    }

                }
            }
        });


        binding.productDetailsBtnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.productDetailsQuantity.setText(String.valueOf(quantity));
            }
        });

        binding.productDetailsBtnPlus.setOnClickListener(v -> {
            if (quantity < avbQuantity) {
                quantity++;
                binding.productDetailsQuantity.setText(String.valueOf(quantity));
            }
        });


        loadTopSellProduct();


        binding.productDetailsBtnAddCart.setOnClickListener(v -> {

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
            } else {

                List<CartItem.Attribute> attributes = getFinalSelections();

                CartItem cartItem = new CartItem(productId, quantity, attributes);

                String uid = firebaseAuth.getCurrentUser().getUid();

                db.collection("users").document(uid).collection("cart").document()
                        .set(cartItem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Item added to cart!", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        });

        checkWishlistStatus();

        binding.productDetailsWishlist.setOnClickListener(v -> {
            toggleWishlist();
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    private void shareProduct() {
        if (currentProduct != null) {
            String shareMessage = "Check out this product on EShop: " + currentProduct.getTitle() + "\n" +
                    "Price: LKR " + currentProduct.getPrice() + "\n" +
                    "Link: https://eshop.lk/product/" + currentProduct.getProductId();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "EShop Product");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
    }

    private void checkWishlistStatus() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            String uid = firebaseAuth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(uid).collection("wishlist")
                    .whereEqualTo("productId", productId)
                    .get()
                    .addOnSuccessListener(qds -> {
                        if (!qds.isEmpty()) {
                            isInWishlist = true;
                            wishlistDocId = qds.getDocuments().get(0).getId();
                            binding.productDetailsWishlist.setChecked(true);
                        } else {
                            isInWishlist = false;
                            binding.productDetailsWishlist.setChecked(false);
                        }
                    });
        }
    }

    private void toggleWishlist() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
            return;
        }

        String uid = firebaseAuth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (isInWishlist) {
            // Remove from wishlist
            db.collection("users").document(uid).collection("wishlist").document(wishlistDocId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        isInWishlist = false;
                        binding.productDetailsWishlist.setChecked(false);
                        Toast.makeText(getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add to wishlist
            Wishlist wishlistItem = new Wishlist(productId);
            db.collection("users").document(uid).collection("wishlist")
                    .add(wishlistItem)
                    .addOnSuccessListener(dr -> {
                        isInWishlist = true;
                        wishlistDocId = dr.getId();
                        binding.productDetailsWishlist.setChecked(true);
                        Toast.makeText(getContext(), "Added to wishlist", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadTopSellProduct() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").whereNotEqualTo("productId", productId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                if (!qds.isEmpty()) {
                    List<Product> products = qds.toObjects(Product.class);


                    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

                    binding.productDetailsTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);


                    SectionAdapter adapter = new SectionAdapter(products, product -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", product.getProductId());

                        ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                        productDetailsFragment.setArguments(bundle);

                        getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, productDetailsFragment).addToBackStack(null).commit();
                    });

                    binding.productDetailsTopSellSection.itemSectionTitle.setText("Top Selling Products");
                    binding.productDetailsTopSellSection.itemSectionContainer.setAdapter(adapter);

                }
            }
        });

    }

    private void renderAttribute(Product.Attribute attribute, ViewGroup container) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);


        //Create Label
        TextView label = new TextView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        label.setLayoutParams(layoutParams);

        label.setText(attribute.getName());

        row.addView(label);

        //Create Options
        ChipGroup group = new ChipGroup(getContext());


        group.setSelectionRequired(true);
        group.setSingleSelection(true);

        attribute.getValues().forEach(value -> {
            Chip chip = new Chip(getContext());
            chip.setCheckable(true);
            chip.setChipStrokeWidth(3f);

            chip.setTag(value);

            if ("color".equals(attribute.getType())) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(value)));
            } else {
                chip.setText(value);
            }

            group.addView(chip);
        });

        row.addView(group);

        container.addView(row);

        attributeGroups.put(attribute.getName(), group);

    }


    private List<CartItem.Attribute> getFinalSelections() {

        List<CartItem.Attribute> attributes = new ArrayList<>();

        for (Map.Entry<String, ChipGroup> entry : attributeGroups.entrySet()) {
            String attributeName = entry.getKey();
            ChipGroup chipGroup = entry.getValue();

            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != -1) {
                Chip chip = getView().findViewById(checkedChipId);
                String value = chip.getTag().toString();

                attributes.add(new CartItem.Attribute(attributeName, value));
            }
        }
        return attributes;
    }


}
