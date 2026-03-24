package lk.jiat.eshop.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

import lk.jiat.eshop.R;
import lk.jiat.eshop.databinding.FragmentInvoiceBinding;
import lk.jiat.eshop.model.Order;

public class InvoiceFragment extends Fragment {

    private FragmentInvoiceBinding binding;
    private Order order;

    public InvoiceFragment() {
        // Required empty public constructor
    }

    public static InvoiceFragment newInstance(Order order) {
        InvoiceFragment fragment = new InvoiceFragment();
        Bundle args = new Bundle();
        args.putSerializable("order", order);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInvoiceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            order = (Order) getArguments().getSerializable("order");
        }

        if (order != null) {
            updateUI();
        }

        binding.invoiceBtnDone.setOnClickListener(v -> {
            // Navigate to Home or maybe NearbyShops as originally intended in CheckoutFragment
            // Let's go to HomeFragment for now as a generic "Done" action
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        });
    }

    private void updateUI() {
        if (order == null || binding == null) return;

        binding.invoiceOrderId.setText(String.format("#%s", order.getOrderId()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        if (order.getOrderDate() != null) {
            // orderDate is now java.util.Date
            binding.invoiceDate.setText(sdf.format(order.getOrderDate()));
        }

        Order.Address address = order.getShippingAddress();
        if (address != null) {
            binding.invoiceCustomerName.setText(address.getName());
            String fullAddress = String.format("%s\n%s%s", 
                    address.getAddress1(),
                    (address.getAddress2() == null || address.getAddress2().isEmpty() ? "" : address.getAddress2() + "\n"),
                    address.getCity());
            binding.invoiceCustomerAddress.setText(fullAddress);
        }

        binding.invoiceItemsContainer.removeAllViews();
        double subtotal = 0;
        if (order.getOrderItems() != null) {
            for (Order.OrderItem item : order.getOrderItems()) {
                View itemView = getLayoutInflater().inflate(R.layout.item_invoice_product, binding.invoiceItemsContainer, false);
                TextView nameTxt = itemView.findViewById(R.id.invoiceItemName);
                TextView qtyTxt = itemView.findViewById(R.id.invoiceItemQty);
                TextView priceTxt = itemView.findViewById(R.id.invoiceItemTotal);

                String displayName = item.getProductTitle() != null ? item.getProductTitle() : "Product ID: " + item.getProductId();
                nameTxt.setText(displayName);
                qtyTxt.setText(String.valueOf(item.getQuantity()));
                double itemTotal = item.getUnitPrice() * item.getQuantity();
                priceTxt.setText(String.format(Locale.US, "LKR %,.2f", itemTotal));
                
                subtotal += itemTotal;
                binding.invoiceItemsContainer.addView(itemView);
            }
        }

        binding.invoiceSubtotal.setText(String.format(Locale.US, "LKR %,.2f", subtotal));
        binding.invoiceTotal.setText(String.format(Locale.US, "LKR %,.2f", order.getTotalAmount()));
    }
}
