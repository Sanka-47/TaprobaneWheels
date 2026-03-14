package lk.jiat.eshop.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.ListingAdapter;
import lk.jiat.eshop.databinding.FragmentSearchBinding;
import lk.jiat.eshop.model.Product;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private String query;
    private String brand;
    private String color;
    private String size;
    private String sort;
    private ListingAdapter adapter;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            query = getArguments().getString("query");
            brand = getArguments().getString("brand");
            color = getArguments().getString("color");
            size = getArguments().getString("size");
            sort = getArguments().getString("sort");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewSearch.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.searchResultsTitle.setText("Search Results for \"" + (query != null ? query : "") + "\"");

        performSearch();
    }

    private void performSearch() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = queryDocumentSnapshots.toObjects(Product.class);
                    List<Product> filteredProducts = new ArrayList<>();
                    
                    String lowerQuery = query != null ? query.toLowerCase() : "";
                    
                    for (Product p : products) {
                        boolean matchesQuery = query == null || query.isEmpty() ||
                            (p.getBrand() != null && p.getBrand().toLowerCase().contains(lowerQuery)) ||
                            (p.getModel() != null && p.getModel().toLowerCase().contains(lowerQuery)) ||
                            (p.getTitle() != null && p.getTitle().toLowerCase().contains(lowerQuery));
                        
                        boolean matchesBrand = brand == null || (p.getBrand() != null && p.getBrand().equalsIgnoreCase(brand));
                        
                        boolean matchesColor = color == null;
                        if (color != null && p.getAttributes() != null) {
                            for (Product.Attribute attr : p.getAttributes()) {
                                if ("Color".equalsIgnoreCase(attr.getName()) && attr.getValues().contains(color)) {
                                    matchesColor = true;
                                    break;
                                }
                            }
                        }

                        boolean matchesSize = size == null;
                        if (size != null && p.getAttributes() != null) {
                            for (Product.Attribute attr : p.getAttributes()) {
                                if ("Rim Size".equalsIgnoreCase(attr.getName()) && attr.getValues().contains(size)) {
                                    matchesSize = true;
                                    break;
                                }
                            }
                        }

                        if (matchesQuery && matchesBrand && matchesColor && matchesSize) {
                            filteredProducts.add(p);
                        }
                    }

                    // Apply Sorting
                    if ("desc".equalsIgnoreCase(sort)) {
                        filteredProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                    } else {
                        filteredProducts.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
                    }

                    if (filteredProducts.isEmpty()) {
                        binding.searchResultsTitle.setText("No results found");
                    } else {
                        binding.searchResultsTitle.setText(filteredProducts.size() + " Results Found");
                    }

                    adapter = new ListingAdapter(filteredProducts, product -> {
                        Bundle bundle = new Bundle();
                        bundle.putString("productId", product.getProductId());

                        ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                        productDetailsFragment.setArguments(bundle);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, productDetailsFragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    binding.recyclerViewSearch.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Search error: " + e.getMessage());
                });
    }
}
