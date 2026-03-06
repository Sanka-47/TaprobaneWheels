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

//        Product p1 = new Product("1", "OZ CRZ", "OZ Wheels are a globally recognized brand of high-performance all...", 4500, "wheel_cat", Arrays.asList("url_13_1", "url_13_2"), 10, true, 0.0f, null);
//        Product p2 = new Product("2", "OZ Monoblocks", "OZ Wheels are a globally recognized brand of high-performance all...", 3500, "wheel_cat", Arrays.asList("url_14_1", "url_14_2"), 6, true, 0.0f, null);
//        Product p3 = new Product("3", "OZ Veruca", "OZ Wheels are a globally recognized brand of high-performance all...", 2500, "wheel_cat", Arrays.asList("url_15_1", "url_15_2"), 7, true, 0.0f, null);
//        Product p4 = new Product("4", "OZ Monoblock CZ", "OZ Wheels are a globally recognized brand of high-performance all...", 6500, "wheel_cat", Arrays.asList("url_16_1", "url_16_2"), 6, true, 0.0f, null);
//        Product p5 = new Product("5", "ADVEN Racing ONI2", "The ADVAN Racing ONI2 Progressive wheel marks a significant ...", 2600, "wheel_cat", Arrays.asList("url_17_1", "url_17_2"), 10, true, 0.0f, null);
//        Product p6 = new Product("6", "BBS SR", "Elegant sportiness - unique BBS design paired with maximumfun...", 452, "wheel_cat", Arrays.asList("url_18_1", "url_18_2"), 0, true, 0.0f, null);
//        Product p7 = new Product("7", "BBS TI-A", "With the TL-A design, BBS breaks new ground once more. Especia...", 552, "wheel_cat", Arrays.asList("url_19_1", "url_19_2"), 3, true, 0.0f, null);
//        Product p8 = new Product("8", "BBS CC-R", "Filigree, light, dynamic, different but typical BBS: A modern desig...", 100, "wheel_cat", Arrays.asList("url_20_1", "url_20_2"), 4, true, 0.0f, null);
//        Product p9 = new Product("9", "BBS SX", "The bestseller re-imagined in a modern double-spoke design.W...", 225, "wheel_cat", Arrays.asList("url_21_1", "url_21_2"), 6, true, 0.0f, null);
//        Product p10 = new Product("10", "BBS LM", "BBS moves another classic wheel into the spotlight with the LM...", 265, "wheel_cat", Arrays.asList("url_22_1", "url_22_2"), 5, true, 0.0f, null);
//        Product p11 = new Product("11", "BBS CIR", "The consistent advancement of the legendary CH wheel. Spor...", 255, "wheel_cat", Arrays.asList("url_23_1", "url_23_2"), 6, true, 0.0f, null);
//        Product p12 = new Product("12", "OZ AERO", "More than 35 years of racing experience have clearly demonstrate...", 244, "wheel_cat", Arrays.asList("url_24_1", "url_24_2"), 10, true, 0.0f, null);
//        Product p13 = new Product("13", "OZ RG", "Superturismo Magnesio is made with the same production technolo...", 513, "wheel_cat", Arrays.asList("url_25_1", "url_25_2"), 9, true, 0.0f, null);
//        Product p14 = new Product("14", "OZ ITALIA", "The ITALIA 150 wheel is OZ's tribute to the 150th anniversary of ...", 221, "wheel_cat", Arrays.asList("url_26_1", "url_26_2"), 6, true, 0.0f, null);
//        Product p15 = new Product("15", "OZ VELOCE", "VELOCE GT, the light alloy wheel for compact cars with supercar ...", 256, "wheel_cat", Arrays.asList("url_27_1", "url_27_2"), 4, true, 0.0f, null);
//        Product p16 = new Product("16", "OZ LEGGENDA", "Leggenda, like the legend of OZ in the world of WRC, is race-inspir...", 152, "wheel_cat", Arrays.asList("url_28_1", "url_28_2"), 7, true, 0.0f, null);
//        Product p17 = new Product("17", "OZ HLT", "The most well-known 6 double-spoke wheel ever! Ultraleggera HL...", 450, "wheel_cat", Arrays.asList("url_29_1", "url_29_2"), 13, true, 0.0f, null);
//        Product p18 = new Product("18", "OZ Elegante", "OZ Wheels are a globally recognized brand of high-performance all...", 4800, "wheel_cat", Arrays.asList("url_30_1", "url_30_2"), 0, true, 0.0f, null);
//        Product p19 = new Product("19", "Venga SRR", "jkajlkfsjamlfd", 456, "wheel_cat", Arrays.asList("url_31_1", "url_31_2"), 10, true, 0.0f, null);
//
//
//        List<Product> list = List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19);
//
//        WriteBatch batch = db.batch();
//
//        for (Product p : list) {
//            DocumentReference ref = db.collection("products").document();
//            batch.set(ref, p);
//        }
//
//        batch.commit();



        db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .orderBy("title", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(ds -> {
                    if (!ds.isEmpty()) {
                        List<Product> products = ds.toObjects(Product.class);

                        adapter = new ListingAdapter(products, product -> {

                            Bundle bundle = new Bundle();
                            bundle.putString("productId",product.getProductId());

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
                        Log.e("Firestore", "Error:"+e.getMessage());
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
