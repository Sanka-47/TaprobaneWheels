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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.CartAdapter;
import lk.jiat.eshop.databinding.FragmentCartBinding;
import lk.jiat.eshop.model.CartItem;
import lk.jiat.eshop.model.Product;


public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private List<CartItem> cartItems = new ArrayList<>();
    private CartAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            String uid = firebaseAuth.getCurrentUser().getUid();

            binding.cartCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new CartAdapter(cartItems);
            binding.cartCartItems.setAdapter(adapter);

            adapter.setOnQuantityChangeListener(cartItem -> {
                db.collection("users").document(uid)
                        .collection("cart")
                        .document(cartItem.getDocumentId())
                        .update("quantity", cartItem.getQuantity())
                        .addOnSuccessListener(aVoid -> {
                            updateTotal();
                        });
            });

            adapter.setOnRemoveListener(position -> {
                String documentId = cartItems.get(position).getDocumentId();
                db.collection("users").document(uid).collection("cart").document(documentId).delete().addOnSuccessListener(aVoid -> {
                    cartItems.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, cartItems.size());
                    updateTotal();
                    checkEmptyCart();
                    Toast.makeText(getContext(), "Item has been removed!", Toast.LENGTH_SHORT).show();
                });
            });

            loadCartItems(uid);
        }

        binding.cartBtnProceed.setOnClickListener(v -> {
            CheckoutFragment checkoutFragment = new CheckoutFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, checkoutFragment)
                    .addToBackStack(null)
                    .commit();
        });

    }

    private void loadCartItems(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).collection("cart").get().addOnSuccessListener(qds -> {
            cartItems.clear();
            for (DocumentSnapshot ds : qds.getDocuments()) {
                CartItem cartItem = ds.toObject(CartItem.class);
                if (cartItem != null) {
                    cartItem.setDocumentId(ds.getId());
                    cartItems.add(cartItem);
                }
            }
            adapter.notifyDataSetChanged();
            updateTotal();
            checkEmptyCart();
        });
    }

    private void checkEmptyCart() {
        if (cartItems.isEmpty()) {
            binding.cartEmptyView.setVisibility(View.VISIBLE);
            binding.cartCartItems.setVisibility(View.GONE);
            binding.cartCheckoutContainer.setVisibility(View.GONE);
        } else {
            binding.cartEmptyView.setVisibility(View.GONE);
            binding.cartCartItems.setVisibility(View.VISIBLE);
            binding.cartCheckoutContainer.setVisibility(View.VISIBLE);
        }
    }

    private void updateTotal() {
        if (cartItems.isEmpty()) {
            binding.cartTextTotal.setText(String.format(Locale.US, "LKR %,.2f", 0.00));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> productIds = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.getProductId() != null) {
                productIds.add(item.getProductId());
            }
        }

        if (productIds.isEmpty()) {
            binding.cartTextTotal.setText(String.format(Locale.US, "LKR %,.2f", 0.00));
            return;
        }

        db.collection("products").whereIn("productId", productIds).get().addOnSuccessListener(qds -> {
            Map<String, Product> productMap = new HashMap<>();
            for (DocumentSnapshot ds : qds.getDocuments()) {
                Product product = ds.toObject(Product.class);
                if (product != null) {
                    productMap.put(product.getProductId(), product);
                }
            }

            double total = 0;
            for (CartItem cartItem : cartItems) {
                Product product = productMap.get(cartItem.getProductId());
                if (product != null) {
                    total += product.getPrice() * cartItem.getQuantity();
                }
            }
            binding.cartTextTotal.setText(String.format(Locale.US, "LKR %,.2f", total));
        });
    }
}
