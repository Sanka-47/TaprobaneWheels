package lk.jiat.eshop.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.WishlistAdapter;
import lk.jiat.eshop.databinding.FragmentWishlistBinding;
import lk.jiat.eshop.model.Wishlist;


public class WishlistFragment extends Fragment {

    private FragmentWishlistBinding binding;
    private List<Wishlist> wishlistItems = new ArrayList<>();
    private WishlistAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWishlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            String uid = firebaseAuth.getCurrentUser().getUid();

            binding.wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new WishlistAdapter(wishlistItems);
            binding.wishlistRecyclerView.setAdapter(adapter);

            adapter.setOnItemRemoveListener(position -> {
                String documentId = wishlistItems.get(position).getDocumentId();
                db.collection("users").document(uid).collection("wishlist").document(documentId).delete()
                        .addOnSuccessListener(aVoid -> {
                            wishlistItems.remove(position);
                            adapter.notifyItemRemoved(position);
                            adapter.notifyItemRangeChanged(position, wishlistItems.size());
                            checkEmptyWishlist();
                            Toast.makeText(getContext(), "Item removed from wishlist", Toast.LENGTH_SHORT).show();
                        });
            });

            adapter.setOnItemViewListener(productId -> {
                Bundle bundle = new Bundle();
                bundle.putString("productId", productId);
                ProductDetailsFragment fragment = new ProductDetailsFragment();
                fragment.setArguments(bundle);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });

            loadWishlistItems(uid);
        }
    }

    private void loadWishlistItems(String uid) {
        db.collection("users").document(uid).collection("wishlist").get().addOnSuccessListener(qds -> {
            wishlistItems.clear();
            for (DocumentSnapshot ds : qds.getDocuments()) {
                Wishlist item = ds.toObject(Wishlist.class);
                if (item != null) {
                    item.setDocumentId(ds.getId());
                    wishlistItems.add(item);
                }
            }
            adapter.notifyDataSetChanged();
            checkEmptyWishlist();
        });
    }

    private void checkEmptyWishlist() {
        if (wishlistItems.isEmpty()) {
            binding.wishlistEmptyView.setVisibility(View.VISIBLE);
            binding.wishlistRecyclerView.setVisibility(View.GONE);
        } else {
            binding.wishlistEmptyView.setVisibility(View.GONE);
            binding.wishlistRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
