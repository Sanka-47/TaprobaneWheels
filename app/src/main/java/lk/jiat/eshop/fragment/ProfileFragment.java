package lk.jiat.eshop.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.ProfileAdapter;
import lk.jiat.eshop.databinding.FragmentProfileBinding;
import lk.jiat.eshop.model.User;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private User currentUser;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadImage(imageUri);
                    }
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ProfileAdapter.setupCitySpinner(getContext(), binding.profileCity);

        loadUserData();

        binding.profileBtnUpdate.setOnClickListener(v -> {
            updateProfile();
        });

        binding.profileImageUpdateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists()) {
                            currentUser = ds.toObject(User.class);
                            if (currentUser != null) {
                                String name = (currentUser.getName() != null && !currentUser.getName().isEmpty()) ? currentUser.getName() : "User";
                                binding.profileName.setText(currentUser.getName());
                                binding.profileEmail.setText(currentUser.getEmail());
                                binding.profileContact.setText(currentUser.getContact());
                                binding.profileAddress1.setText(currentUser.getAddress1());
                                binding.profileAddress2.setText(currentUser.getAddress2());
                                binding.profileCity.setText(currentUser.getCity(), false);
                                binding.profilePostalCode.setText(currentUser.getPostalCode());

                                if (currentUser.getProfilePicUrl() != null && !currentUser.getProfilePicUrl().isEmpty()) {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    storage.getReference("profile-images/" + currentUser.getProfilePicUrl()).getDownloadUrl()
                                            .addOnSuccessListener(uri -> {
                                                if (isAdded()) {
                                                    Glide.with(ProfileFragment.this)
                                                            .load(uri)
                                                            .circleCrop()
                                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                            .placeholder(R.drawable.person_24)
                                                            .into(binding.profileImage);
                                                }
                                            })
                                            .addOnFailureListener(e -> loadDynamicAvatar(name));
                                } else {
                                    loadDynamicAvatar(name);
                                }
                            }
                        }
                    });
        }
    }

    private void loadDynamicAvatar(String name) {
        // background=001F3F matches your md_theme_primary
        String initialsUrl = "https://ui-avatars.com/api/?name=" + Uri.encode(name) + "&background=001F3F&color=fff&size=256&bold=true";
        if (isAdded()) {
            Glide.with(ProfileFragment.this)
                    .load(initialsUrl)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.person_24)
                    .error(R.drawable.person_24)
                    .into(binding.profileImage);
        }
    }

    private void uploadImage(Uri imageUri) {
        if (currentUser == null) return;

        String imageId = UUID.randomUUID().toString();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference("profile-images/" + imageId);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    db.collection("users").document(currentUser.getUid()).update("profilePicUrl", imageId)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Profile picture updated", Toast.LENGTH_SHORT).show();
                                loadUserData();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfile() {
        if (currentUser == null) return;

        String name = binding.profileName.getText().toString().trim();
        String contact = binding.profileContact.getText().toString().trim();
        String address1 = binding.profileAddress1.getText().toString().trim();
        String address2 = binding.profileAddress2.getText().toString().trim();
        String city = binding.profileCity.getText().toString().trim();
        String postalCode = binding.profilePostalCode.getText().toString().trim();

        if (name.isEmpty()) {
            binding.profileName.setError("Name is required");
            return;
        }

        if (contact.isEmpty()) {
            binding.profileContact.setError("Required");
            return;
        }

        currentUser.setName(name);
        currentUser.setContact(contact);
        currentUser.setAddress1(address1);
        currentUser.setAddress2(address2);
        currentUser.setCity(city);
        currentUser.setPostalCode(postalCode);

        db.collection("users").document(currentUser.getUid()).set(currentUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    loadUserData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
