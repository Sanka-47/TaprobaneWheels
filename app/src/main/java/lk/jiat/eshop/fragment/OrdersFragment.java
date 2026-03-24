package lk.jiat.eshop.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.OrderAdapter;
import lk.jiat.eshop.databinding.FragmentOrdersBinding;
import lk.jiat.eshop.model.Order;


public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();
    private OrderAdapter adapter;
    private boolean isAscending = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(filteredOrders);
        
        adapter.setOnOrderClickListener(order -> {
            InvoiceFragment invoiceFragment = InvoiceFragment.newInstance(order);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, invoiceFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.ordersRecyclerView.setAdapter(adapter);

        setupSearch();
        setupSorting();
        loadOrders();
    }

    private void setupSearch() {
        binding.searchOrderEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterOrders(String query) {
        filteredOrders.clear();
        if (query.isEmpty()) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (order.getOrderId().toLowerCase().contains(query.toLowerCase())) {
                    filteredOrders.add(order);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupSorting() {
        binding.btnSortOrders.setOnClickListener(v -> {
            isAscending = !isAscending;
            sortOrders();
            binding.btnSortOrders.setRotation(isAscending ? 180f : 0f);
        });
    }

    private void sortOrders() {
        Collections.sort(filteredOrders, (o1, o2) -> {
            if (o1.getOrderDate() == null || o2.getOrderDate() == null) return 0;
            if (isAscending) {
                return o1.getOrderDate().compareTo(o2.getOrderDate());
            } else {
                return o2.getOrderDate().compareTo(o1.getOrderDate());
            }
        });
        adapter.notifyDataSetChanged();
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
                        allOrders.clear();
                        for (DocumentSnapshot ds : qds.getDocuments()) {
                            Order order = ds.toObject(Order.class);
                            if (order != null) {
                                allOrders.add(order);
                            }
                        }
                        filteredOrders.clear();
                        filteredOrders.addAll(allOrders);
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                        db.collection("orders")
                                .whereEqualTo("userId", uid)
                                .get()
                                .addOnSuccessListener(qds -> {
                                    allOrders.clear();
                                    for (DocumentSnapshot ds : qds.getDocuments()) {
                                        Order order = ds.toObject(Order.class);
                                        if (order != null) {
                                            allOrders.add(order);
                                        }
                                    }
                                    filteredOrders.clear();
                                    filteredOrders.addAll(allOrders);
                                    adapter.notifyDataSetChanged();
                                });
                    });
        }
    }
}
