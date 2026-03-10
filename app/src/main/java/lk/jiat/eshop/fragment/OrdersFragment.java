package lk.jiat.eshop.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.eshop.adapter.OrderAdapter;
import lk.jiat.eshop.databinding.FragmentOrdersBinding;
import lk.jiat.eshop.model.Order;


public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private List<Order> orders = new ArrayList<>();
    private OrderAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(orders);
        binding.ordersRecyclerView.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            String uid = firebaseAuth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("orders")
                    .whereEqualTo("userId", uid)
                    .orderBy("orderDate", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(qds -> {
                        orders.clear();
                        for (DocumentSnapshot ds : qds.getDocuments()) {
                            Order order = ds.toObject(Order.class);
                            if (order != null) {
                                orders.add(order);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                        // Handle failure, e.g., if index is missing
                        db.collection("orders")
                                .whereEqualTo("userId", uid)
                                .get()
                                .addOnSuccessListener(qds -> {
                                    orders.clear();
                                    for (DocumentSnapshot ds : qds.getDocuments()) {
                                        Order order = ds.toObject(Order.class);
                                        if (order != null) {
                                            orders.add(order);
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                });
                    });
        }
    }
}
