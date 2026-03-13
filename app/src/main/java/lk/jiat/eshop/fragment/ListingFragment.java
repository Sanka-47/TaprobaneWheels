package lk.jiat.eshop.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.ListingAdapter;
import lk.jiat.eshop.databinding.FragmentListingBinding;
import lk.jiat.eshop.model.Product;

public class ListingFragment extends Fragment {

    private FragmentListingBinding binding;
    private ListingAdapter adapter;
    private String categoryId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.recyclerViewListing.setLayoutManager(new GridLayoutManager(getContext(), 2));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

//
//        // Data update logic for Wheels
//        Product.Attribute colorAttr = new Product.Attribute("Color", "color", List.of("#000000", "#C0C0C0", "#FFD700"));
//        Product.Attribute rimSizeAttr = new Product.Attribute("Rim Size", "text", List.of("17\"", "18\"", "19\""));
//
//        Product p1 = new Product("1", "OZ", "CRZ", "OZ Wheels are a globally recognized brand of high-performance all...", 4500, "cat1", Arrays.asList("product-images/1/1.png", "product-images/1/1.png"), 10, true);
//        p1.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p1.setRating(4.5f);
//
//        Product p2 = new Product("2", "OZ", "Monoblocks", "OZ Wheels are a globally recognized brand of high-performance all...", 3500, "cat1", Arrays.asList("product-images/2/1.png", "product-images/2/1.png"), 6, true);
//        p2.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p2.setRating(4.0f);
//
//        Product p3 = new Product("3", "OZ", "Veruca", "OZ Wheels are a globally recognized brand of high-performance all...", 2500, "cat1", Arrays.asList("product-images/3/1.png", "product-images/3/1.png"), 7, true);
//        p3.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p3.setRating(4.0f);
//
//        Product p4 = new Product("4", "OZ", "Monoblock CZ", "OZ Wheels are a globally recognized brand of high-performance all...", 6500, "cat1", Arrays.asList("product-images/4/1.png", "product-images/4/1.png"), 6, true);
//        p4.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p4.setRating(4.0f);
//
//        Product p5 = new Product("5", "ADVAN", "Racing ONI2", "The ADVAN Racing ONI2 Progressive wheel marks a significant ...", 2600, "cat2", Arrays.asList("product-images/5/1.png", "product-images/5/1.png"), 10, true);
//        p5.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p5.setRating(4.8f);
//
//        Product p6 = new Product("6", "BBS", "SR", "Elegant sportiness - unique BBS design paired with maximumfun...", 452, "cat1", Arrays.asList("product-images/6/1.png", "product-images/6/1.png"), 0, true);
//        p6.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p6.setRating(4.2f);
//
//        Product p7 = new Product("7", "BBS", "TI-A", "With the TL-A design, BBS breaks new ground once more. Especia...", 552, "cat1", Arrays.asList("product-images/7/1.png", "product-images/7/1.png"), 3, true);
//        p7.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p7.setRating(4.0f);
//
//        Product p8 = new Product("8", "BBS", "CC-R", "Filigree, light, dynamic, different but typical BBS: A modern desig...", 100, "cat1", Arrays.asList("product-images/8/1.png", "product-images/8/1.png"), 4, true);
//        p8.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p8.setRating(4.0f);
//
//        Product p9 = new Product("9", "BBS", "SX", "The bestseller re-imagined in a modern double-spoke design.W...", 225, "cat1", Arrays.asList("product-images/9/1.png", "product-images/9/1.png"), 6, true);
//        p9.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p9.setRating(4.0f);
//
//        Product p10 = new Product("10", "BBS", "LM", "BBS moves another classic wheel into the spotlight with the LM...", 265, "cat1", Arrays.asList("product-images/10/1.png", "product-images/10/1.png"), 5, true);
//        p10.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p10.setRating(4.0f);
//
//        Product p11 = new Product("11", "BBS", "CIR", "The consistent advancement of the legendary CH wheel. Spor...", 255, "cat1", Arrays.asList("product-images/11/1.png", "product-images/11/1.png"), 6, true);
//        p11.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p11.setRating(4.0f);
//
//        Product p12 = new Product("12", "OZ", "AERO", "More than 35 years of racing experience have clearly demonstrate...", 244, "cat3", Arrays.asList("product-images/12/1.png", "product-images/12/1.png"), 10, true);
//        p12.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p12.setRating(4.0f);
//
//        Product p13 = new Product("13", "OZ", "RG", "Superturismo Magnesio is made with the same production technolo...", 513, "cat3", Arrays.asList("product-images/13/1.png", "product-images/13/1.png"), 9, true);
//        p13.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p13.setRating(4.0f);
//
//        Product p14 = new Product("14", "OZ", "ITALIA", "The ITALIA 150 wheel is OZ's tribute to the 150th anniversary of ...", 221, "cat3", Arrays.asList("product-images/14/1.png", "product-images/14/1.png"), 6, true);
//        p14.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p14.setRating(4.0f);
//
//        Product p15 = new Product("15", "OZ", "VELOCE", "VELOCE GT, the light alloy wheel for compact cars with supercar ...", 256, "cat3", Arrays.asList("product-images/15/1.png", "product-images/15/1.png"), 4, true);
//        p15.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p15.setRating(4.0f);
//
//        Product p16 = new Product("16", "OZ", "LEGGENDA", "Leggenda, like the legend of OZ in the world of WRC, is race-inspir...", 152, "cat3", Arrays.asList("product-images/16/1.png", "product-images/16/1.png"), 7, true);
//        p16.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p16.setRating(4.0f);
//
//        Product p17 = new Product("17", "OZ", "HLT", "The most well-known 6 double-spoke wheel ever! Ultraleggera HL...", 450, "cat3", Arrays.asList("product-images/17/1.png", "product-images/17/1.png"), 13, true);
//        p17.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p17.setRating(4.0f);
//
//        Product p18 = new Product("18", "OZ", "Elegante", "OZ Wheels are a globally recognized brand of high-performance all...", 4800, "cat3", Arrays.asList("product-images/18/1.png", "product-images/18/1.png"), 0, true);
//        p18.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p18.setRating(4.0f);
//
//        Product p19 = new Product("19", "Venga", "SRR", "jkajlkfsjamlfd", 456, "cat4", Arrays.asList("product-images/19/1.png", "product-images/19/1.png"), 10, true);
//        p19.setAttributes(List.of(colorAttr, rimSizeAttr));
//        p19.setRating(4.0f);
//
//        List<Product> list = List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19);
//
//        WriteBatch batch = db.batch();
//        for (Product p : list) {
//            DocumentReference ref = db.collection("products").document(p.getProductId());
//            batch.set(ref, p);
//        }
//        batch.commit().addOnSuccessListener(aVoid -> Log.d("Firestore", "All products updated"));







        db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(ds -> {
                    if (!ds.isEmpty()) {
                        List<Product> products = ds.toObjects(Product.class);

                        adapter = new ListingAdapter(products, product -> {

                            Bundle bundle = new Bundle();
                            bundle.putString("productId", product.getProductId());

                            ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                            productDetailsFragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, productDetailsFragment)
                                    .addToBackStack(null)
                                    .commit();
                        });

                        binding.recyclerViewListing.setAdapter(adapter);
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error:" + e.getMessage());
                    }
                });

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

    }
}
