package lk.jiat.eshop.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.model.Product;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {

    private List<Product> products;
    private OnListingItemClickListener listener;
    private FirebaseStorage storage;

    public SectionAdapter(List<Product> products, OnListingItemClickListener listener) {
        this.products = products;
        this.listener = listener;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public SectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_recycler, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionAdapter.ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.productTitle.setText(product.getTitle());
        holder.productPrice.setText("LKR " + product.getPrice());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String imagePath = product.getImages().get(0);

            if (imagePath.startsWith("http")) {
                Glide.with(holder.itemView.getContext())
                        .load(imagePath)
                        .centerCrop()
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
                            Context context = holder.itemView.getContext();
                            if (isValidContextForGlide(context)) {
                                Glide.with(context)
                                        .load(uri)
                                        .centerCrop()
                                        .into(holder.productImage);
                            }
                        });
            }
        }

        holder.itemView.setOnClickListener(v -> {

            Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.click_animation);
            v.startAnimation(animation);

            if (listener != null) {
                listener.onListingItemClick(product);
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
        return products.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productTitle;
        TextView productPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.item_product_r_image);
            productTitle = itemView.findViewById(R.id.item_product_r_name);
            productPrice = itemView.findViewById(R.id.item_product_r_price);
        }
    }

    public interface OnListingItemClickListener {
        void onListingItemClick(Product product);
    }
}
