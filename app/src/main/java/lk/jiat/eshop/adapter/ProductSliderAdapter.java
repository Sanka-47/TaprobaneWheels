package lk.jiat.eshop.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import lk.jiat.eshop.R;

public class ProductSliderAdapter extends RecyclerView.Adapter<ProductSliderAdapter.ProductSliderViewHolder> {

    private List<String> images;
    private FirebaseStorage storage;

    public ProductSliderAdapter(List<String> images) {
        this.images = images;
        this.storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ProductSliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_slider_item, parent, false);
        return new ProductSliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductSliderViewHolder holder, int position) {
        String imagePath = images.get(position);

        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("http")) {
                Glide.with(holder.itemView.getContext())
                        .load(imagePath)
                        .centerCrop()
                        .into(holder.imageView);
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
                                        .into(holder.imageView);
                            }
                        });
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
        return images.size();
    }


    public static class ProductSliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ProductSliderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.product_slider_item_image);
        }
    }
}
