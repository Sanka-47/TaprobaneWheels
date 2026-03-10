package lk.jiat.eshop.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;

import lk.jiat.eshop.R;
import lk.jiat.eshop.model.CartItem;
import lk.jiat.eshop.model.Product;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems;
    private OnQuantityChangeListener changeListener;
    private OnRemoveListener removeListener;
    private FirebaseStorage storage;

    public CartAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        this.storage = FirebaseStorage.getInstance();
    }

    public void setOnQuantityChangeListener(OnQuantityChangeListener listener) {
        this.changeListener = listener;
    }

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.ViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        // Reset UI to prevent showing old or default data
        holder.productTitle.setText("Loading...");
        holder.productPrice.setText("");
        holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
        holder.productImage.setImageResource(R.drawable.app_logo); // Using app_logo as placeholder
        holder.itemView.setVisibility(View.VISIBLE);
        holder.itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        holder.itemView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products").whereEqualTo("productId", cartItem.getProductId()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {
                int currentPosition = holder.getAbsoluteAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) {
                    return;
                }

                if (!qds.isEmpty()) {
                    Product product = qds.getDocuments().get(0).toObject(Product.class);

                    holder.productTitle.setText(product.getTitle());
                    holder.productPrice.setText(String.format(Locale.US, "LKR %,.2f", product.getPrice()));
                    holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));

                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        String imagePath = product.getImages().get(0);

                        if (imagePath.startsWith("http")) {
                            Glide.with(holder.itemView.getContext())
                                    .load(imagePath)
                                    .centerCrop()
                                    .placeholder(R.drawable.app_logo)
                                    .into(holder.productImage);
                        } else {
                            StorageReference storageReference;
                            if (imagePath.startsWith("gs://")) {
                                storageReference = storage.getReferenceFromUrl(imagePath);
                            } else {
                                storageReference = storage.getReference(imagePath);
                            }

                            storageReference.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        Glide.with(holder.itemView.getContext())
                                                .load(uri)
                                                .centerCrop()
                                                .placeholder(R.drawable.app_logo)
                                                .into(holder.productImage);
                                    });
                        }
                    }


                    holder.btnPlus.setOnClickListener(v -> {
                        if (cartItem.getQuantity() < product.getStockCount()) {
                            cartItem.setQuantity(cartItem.getQuantity() + 1);
                            notifyItemChanged(currentPosition);
                            if (changeListener != null) {
                                changeListener.onChanged(cartItem);
                            }
                        }
                    });

                    holder.btnMinus.setOnClickListener(v -> {
                        if (cartItem.getQuantity() > 1) {
                            cartItem.setQuantity(cartItem.getQuantity() - 1);
                            notifyItemChanged(currentPosition);
                            if (changeListener != null) {
                                changeListener.onChanged(cartItem);
                            }
                        }

                    });

                    holder.btnRemove.setOnClickListener(v -> {
                        if (removeListener != null) {
                            removeListener.onRemoved(currentPosition);
                        }
                    });

                } else {
                    // Product not found in database, hide this item
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.getLayoutParams().height = 0;
                    holder.itemView.getLayoutParams().width = 0;
                    Log.e("CartAdapter", "Product not found for ID: " + cartItem.getProductId());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle;
        TextView productPrice;
        TextView productQuantity;
        AppCompatButton btnPlus;
        AppCompatButton btnMinus;
        ImageView btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.item_cart_image);
            productTitle = itemView.findViewById(R.id.item_cart_title);
            productPrice = itemView.findViewById(R.id.item_cart_price);
            productQuantity = itemView.findViewById(R.id.item_cart_quantity);
            btnPlus = itemView.findViewById(R.id.item_cart_btn_plus);
            btnMinus = itemView.findViewById(R.id.item_cart_btn_minus);
            btnRemove = itemView.findViewById(R.id.item_cart_remove);
        }
    }

    public interface OnQuantityChangeListener {
        void onChanged(CartItem cartItem);
    }

    public interface OnRemoveListener {
        void onRemoved(int position);
    }
}
