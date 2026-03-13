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

import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.ListingAdapter;
import lk.jiat.eshop.databinding.FragmentSearchBinding;
import lk.jiat.eshop.model.Product;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private String query;
    private ListingAdapter adapter;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            query = getArguments().getString("query");
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
        binding.searchResultsTitle.setText("Search Results for \"" + query + "\"");

        performSearch();
    }

    private void performSearch() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Simple search logic: match brand or model (Firestore doesn't support full-text search easily)
        // Here we'll search by brand first as an example. 
        // For better search, you'd usually use Algolia or similar, or fetch all and filter locally.
        
        db.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> products = queryDocumentSnapshots.toObjects(Product.class);
                    
                    // Filter locally for better matching (case insensitive)
                    List<Product> filteredProducts = new java.util.ArrayList<>();
                    String lowerQuery = query.toLowerCase();
                    
                    for (Product p : products) {
                        if ((p.getBrand() != null && p.getBrand().toLowerCase().contains(lowerQuery)) ||
                            (p.getModel() != null && p.getModel().toLowerCase().contains(lowerQuery)) ||
                            (p.getTitle() != null && p.getTitle().toLowerCase().contains(lowerQuery))) {
                            filteredProducts.add(p);
                        }
                    }

                    if (filteredProducts.isEmpty()) {
                        binding.searchResultsTitle.setText("No results found for \"" + query + "\"");
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
