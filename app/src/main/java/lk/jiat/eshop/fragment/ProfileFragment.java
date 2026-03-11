package lk.jiat.eshop.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.ProfileAdapter;
import lk.jiat.eshop.databinding.FragmentProfileBinding;
import lk.jiat.eshop.model.User;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private User currentUser;

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
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists()) {
                            currentUser = ds.toObject(User.class);
                            if (currentUser != null) {
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
                                                            .placeholder(R.drawable.person_24)
                                                            .into(binding.profileImage);
                                                }
                                            });
                                }
                            }
                        }
                    });
        }
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
