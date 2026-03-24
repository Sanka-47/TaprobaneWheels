package lk.jiat.eshop.fragment;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import lk.jiat.eshop.R;
import lk.jiat.eshop.databinding.FragmentInvoiceBinding;
import lk.jiat.eshop.model.Order;

public class InvoiceFragment extends Fragment {

    private FragmentInvoiceBinding binding;
    private Order order;

    private final ActivityResultLauncher<String> createDocumentLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/pdf"),
            uri -> {
                if (uri != null) {
                    savePdf(uri);
                }
            }
    );

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

        binding.invoiceBtnSavePdf.setOnClickListener(v -> {
            String fileName = "Invoice_" + (order != null ? order.getOrderId() : System.currentTimeMillis()) + ".pdf";
            createDocumentLauncher.launch(fileName);
        });

        binding.invoiceBtnDone.setOnClickListener(v -> {
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

    private void savePdf(Uri uri) {
        View content = binding.invoiceContentLayout;
        
        // Create a PDF Document
        PdfDocument pdfDocument = new PdfDocument();
        
        // Define page info based on the content view dimensions
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(content.getWidth(), content.getHeight(), 1).create();
        
        // Start a page
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        
        Canvas canvas = page.getCanvas();
        content.draw(canvas);
        
        // Finish the page
        pdfDocument.finishPage(page);

        try {
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                pdfDocument.writeTo(outputStream);
                outputStream.close();
                Toast.makeText(getContext(), "Invoice saved as PDF", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("InvoiceFragment", "Error saving PDF: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to save PDF", Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }
}
