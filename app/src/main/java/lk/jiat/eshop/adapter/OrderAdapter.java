package lk.jiat.eshop.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import lk.jiat.eshop.R;
import lk.jiat.eshop.model.Order;
import lk.jiat.eshop.model.Product;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<Order> orders;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);

        holder.orderId.setText("Order #" + order.getOrderId());
        holder.orderStatus.setText(order.getStatus());
        holder.orderTotal.setText(String.format(Locale.US, "LKR %,.2f", order.getTotalAmount()));
        holder.shippingStatus.setText(order.getShippingStatus());

        if (order.getOrderDate() != null) {
            holder.orderDate.setText(dateFormat.format(order.getOrderDate().toDate()));
        } else {
            holder.orderDate.setText("N/A");
        }

        // Clear previous items to avoid duplication in recycled views
        holder.itemsContainer.removeAllViews();

        if (order.getOrderItems() != null) {
            for (Order.OrderItem item : order.getOrderItems()) {
                View productView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_order_product, holder.itemsContainer, false);

                TextView titleTv = productView.findViewById(R.id.item_order_product_title);
                TextView qtyPriceTv = productView.findViewById(R.id.item_order_product_qty_price);
                ImageView imageView = productView.findViewById(R.id.item_order_product_image);

                qtyPriceTv.setText(String.format(Locale.US, "Qty: %d x LKR %,.2f", item.getQuantity(), item.getUnitPrice()));

                // Load product details from Firestore to get title and image
                db.collection("products").whereEqualTo("productId", item.getProductId()).get()
                        .addOnSuccessListener(qds -> {
                            if (!qds.isEmpty()) {
                                Product product = qds.getDocuments().get(0).toObject(Product.class);
                                if (product != null) {
                                    titleTv.setText(product.getTitle());
                                    
                                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                                        String imagePath = product.getImages().get(0);
                                        if (imagePath.startsWith("http")) {
                                            Context context = holder.itemView.getContext();
                                            if (isValidContextForGlide(context)) {
                                                Glide.with(context)
                                                        .load(imagePath)
                                                        .placeholder(R.drawable.app_logo)
                                                        .into(imageView);
                                            }
                                        } else {
                                            StorageReference ref = imagePath.startsWith("gs://") ? 
                                                    storage.getReferenceFromUrl(imagePath) : storage.getReference(imagePath);
                                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                                Context context = holder.itemView.getContext();
                                                if (isValidContextForGlide(context)) {
                                                    Glide.with(context)
                                                            .load(uri)
                                                            .placeholder(R.drawable.app_logo)
                                                            .into(imageView);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        });

                holder.itemsContainer.addView(productView);
            }
        }
    }

    private boolean isValidContextForGlide(Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderStatus, orderDate, orderTotal, shippingStatus;
        LinearLayout itemsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.item_order_id);
            orderStatus = itemView.findViewById(R.id.item_order_status);
            orderDate = itemView.findViewById(R.id.item_order_date);
            orderTotal = itemView.findViewById(R.id.item_order_total);
            shippingStatus = itemView.findViewById(R.id.item_order_shipping_status);
            itemsContainer = itemView.findViewById(R.id.item_order_items_container);
        }
    }
}
