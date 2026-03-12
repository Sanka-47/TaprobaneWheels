package lk.jiat.eshop.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Locale;

import lk.jiat.eshop.R;
import lk.jiat.eshop.model.Product;
import lk.jiat.eshop.model.Wishlist;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<Wishlist> wishlistItems;
    private OnItemRemoveListener removeListener;
    private OnItemViewListener viewListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public WishlistAdapter(List<Wishlist> wishlistItems) {
        this.wishlistItems = wishlistItems;
    }

    public void setOnItemRemoveListener(OnItemRemoveListener listener) {
        this.removeListener = listener;
    }

    public void setOnItemViewListener(OnItemViewListener listener) {
        this.viewListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wishlist wishlistItem = wishlistItems.get(position);

        db.collection("products").whereEqualTo("productId", wishlistItem.getProductId()).get()
                .addOnSuccessListener(qds -> {
                    if (!qds.isEmpty()) {
                        Product product = qds.getDocuments().get(0).toObject(Product.class);
                        if (product != null) {
                            holder.title.setText(product.getTitle());
                            holder.price.setText(String.format(Locale.US, "LKR %,.2f", product.getPrice()));

                            if (product.getImages() != null && !product.getImages().isEmpty()) {
                                String imagePath = product.getImages().get(0);
                                if (imagePath.startsWith("http")) {
                                    Context context = holder.itemView.getContext();
                                    if (isValidContextForGlide(context)) {
                                        Glide.with(context)
                                                .load(imagePath)
                                                .placeholder(R.drawable.app_logo)
                                                .into(holder.image);
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
                                                    .into(holder.image);
                                        }
                                    });
                                }
                            }

                            holder.btnView.setOnClickListener(v -> {
                                if (viewListener != null) {
                                    viewListener.onView(product.getProductId());
                                }
                            });
                        }
                    }
                });

        holder.btnRemove.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(position);
            }
        });
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
        return wishlistItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image, btnRemove;
        TextView title, price;
        MaterialButton btnView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_wishlist_image);
            title = itemView.findViewById(R.id.item_wishlist_title);
            price = itemView.findViewById(R.id.item_wishlist_price);
            btnRemove = itemView.findViewById(R.id.item_wishlist_remove);
            btnView = itemView.findViewById(R.id.item_wishlist_add_to_cart);
        }
    }

    public interface OnItemRemoveListener {
        void onRemove(int position);
    }

    public interface OnItemViewListener {
        void onView(String productId);
    }
}
