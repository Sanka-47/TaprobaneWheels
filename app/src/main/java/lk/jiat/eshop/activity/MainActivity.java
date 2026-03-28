package lk.jiat.eshop.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import lk.jiat.eshop.R;
import lk.jiat.eshop.databinding.ActivityMainBinding;
import lk.jiat.eshop.databinding.SideNavHeaderBinding;
import lk.jiat.eshop.fragment.CartFragment;
import lk.jiat.eshop.fragment.CategoryFragment;
import lk.jiat.eshop.fragment.HomeFragment;
import lk.jiat.eshop.fragment.ListingFragment;
import lk.jiat.eshop.fragment.MessageFragment;
import lk.jiat.eshop.fragment.NearbyShopsFragment;
import lk.jiat.eshop.fragment.OrdersFragment;
import lk.jiat.eshop.fragment.ProductDetailsFragment;
import lk.jiat.eshop.fragment.ProfileFragment;
import lk.jiat.eshop.fragment.SearchFragment;
import lk.jiat.eshop.fragment.SettingsFragment;
import lk.jiat.eshop.fragment.WishlistFragment;
import lk.jiat.eshop.model.Product;
import lk.jiat.eshop.model.User;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnItemSelectedListener {


    private ActivityMainBinding binding;
    private SideNavHeaderBinding sideNavHeaderBinding;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View headerView = binding.sideNavigationView.getHeaderView(0);
        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

        drawerLayout = binding.drawerLayout;
        toolbar = binding.toolbar;
        navigationView = binding.sideNavigationView;
        bottomNavigationView = binding.bottomNavigationView;

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStack();
                    } else {
                        finish();
                    }
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            updateBottomNavCheckedItem();
        });


        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), false);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        updateSideNavHeader();

        // Search Functionality
        binding.textInputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = binding.textInputSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query, null, null, null, "asc");
                }
                return true;
            }
            return false;
        });

        binding.btnFilter.setOnClickListener(v -> {
            showAdvancedSearchDialog();
        });
    }

    private void showAdvancedSearchDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_advanced_search, null);
        Spinner spinnerBrand = dialogView.findViewById(R.id.spinner_brand);
        Spinner spinnerColor = dialogView.findViewById(R.id.spinner_color);
        Spinner spinnerSize = dialogView.findViewById(R.id.spinner_size);
        View btnApply = dialogView.findViewById(R.id.btn_apply_search);
        android.widget.RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_price);

        // Fetch dynamic filter options from Firestore
        firebaseFirestore.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Product> products = queryDocumentSnapshots.toObjects(Product.class);
            Set<String> brands = new HashSet<>(Collections.singletonList("All Brands"));
            Set<String> colors = new HashSet<>(Collections.singletonList("All Colors"));
            Set<String> sizes = new HashSet<>(Collections.singletonList("All Sizes"));

            for (Product p : products) {
                if (p.getBrand() != null) brands.add(p.getBrand());
                if (p.getAttributes() != null) {
                    for (Product.Attribute attr : p.getAttributes()) {
                        if ("Color".equalsIgnoreCase(attr.getName())) colors.addAll(attr.getValues());
                        if ("Rim Size".equalsIgnoreCase(attr.getName())) sizes.addAll(attr.getValues());
                    }
                }
            }

            setupSpinner(spinnerBrand, new ArrayList<>(brands));
            setupColorSpinner(spinnerColor, new ArrayList<>(colors));
            setupSpinner(spinnerSize, new ArrayList<>(sizes));
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnApply.setOnClickListener(v -> {
            String brand = spinnerBrand.getSelectedItem().toString();
            String color = spinnerColor.getSelectedItem().toString();
            String size = spinnerSize.getSelectedItem().toString();
            String sort = radioGroup.getCheckedRadioButtonId() == R.id.radio_high_low ? "desc" : "asc";

            performSearch(binding.textInputSearch.getText().toString().trim(), 
                    brand.equals("All Brands") ? null : brand,
                    color.equals("All Colors") ? null : color,
                    size.equals("All Sizes") ? null : size,
                    sort);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupColorSpinner(Spinner spinner, List<String> items) {
        ColorSpinnerAdapter adapter = new ColorSpinnerAdapter(this, items);
        spinner.setAdapter(adapter);
    }

    private void performSearch(String query, String brand, String color, String size, String sort) {
        Bundle bundle = new Bundle();
        bundle.putString("query", query);
        bundle.putString("brand", brand);
        bundle.putString("color", color);
        bundle.putString("size", size);
        bundle.putString("sort", sort);

        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(bundle);
        loadFragment(searchFragment, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSideNavHeader();
    }

    private void updateSideNavHeader() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        
        if (currentUser != null) {
            // Initial UI state while loading Firestore
            String displayName = currentUser.getDisplayName();
            sideNavHeaderBinding.headerUserName.setText(displayName != null && !displayName.isEmpty() ? displayName : "User");
            sideNavHeaderBinding.headerUserEmail.setText(currentUser.getEmail());

            firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            if (user != null) {
                                String nameForAvatar = user.getName() != null ? user.getName() : "User";
                                sideNavHeaderBinding.headerUserName.setText(nameForAvatar);
                                sideNavHeaderBinding.headerUserEmail.setText(user.getEmail());

                                if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    storage.getReference("profile-images/" + user.getProfilePicUrl()).getDownloadUrl()
                                            .addOnSuccessListener(uri -> {
                                                if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
                                                    sideNavHeaderBinding.headerProfilePic.setVisibility(View.VISIBLE);
                                                    sideNavHeaderBinding.headerProfilePicDynamic.setVisibility(View.GONE);

                                                    Glide.with(MainActivity.this)
                                                            .load(uri)
                                                            .circleCrop()
                                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                            .placeholder(R.drawable.person_24)
                                                            .into(sideNavHeaderBinding.headerProfilePic);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                loadDynamicAvatar(nameForAvatar);
                                            });
                                } else {
                                    loadDynamicAvatar(nameForAvatar);
                                }
                            }
                        } else {
                            Log.e("Firestore", "Document does not exist for UID: " + currentUser.getUid());
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("Firestore", "Error: " + e.getMessage());
                    });

            // Update Menu Visibility
            setMenuItemVisible(R.id.side_nav_login, false);
            setMenuItemVisible(R.id.side_nav_profile, true);
            setMenuItemVisible(R.id.side_nav_orders, true);
            setMenuItemVisible(R.id.side_nav_wishlist, true);
            setMenuItemVisible(R.id.side_nav_cart, true);
            setMenuItemVisible(R.id.side_nav_nearby_shops, true);
            setMenuItemVisible(R.id.side_nav_message, true);
            setMenuItemVisible(R.id.side_nav_logout, true);

            View.OnClickListener profileClickListener = v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);
            };
            sideNavHeaderBinding.headerProfilePic.setOnClickListener(profileClickListener);
            sideNavHeaderBinding.headerProfilePicDynamic.setOnClickListener(profileClickListener);
        } else {
            sideNavHeaderBinding.headerUserName.setText("You're not logged in");
            sideNavHeaderBinding.headerUserEmail.setText("");
            
            sideNavHeaderBinding.headerProfilePic.setVisibility(View.GONE);
            sideNavHeaderBinding.headerProfilePicDynamic.setVisibility(View.VISIBLE);
            sideNavHeaderBinding.headerProfilePicDynamic.setImageResource(R.drawable.person_24);
            
            setMenuItemVisible(R.id.side_nav_login, true);
            setMenuItemVisible(R.id.side_nav_profile, false);
            setMenuItemVisible(R.id.side_nav_orders, false);
            setMenuItemVisible(R.id.side_nav_wishlist, false);
            setMenuItemVisible(R.id.side_nav_cart, false);
            setMenuItemVisible(R.id.side_nav_nearby_shops, true);
            setMenuItemVisible(R.id.side_nav_message, false);
            setMenuItemVisible(R.id.side_nav_logout, false);
        }
    }

    private void setMenuItemVisible(int id, boolean visible) {
        if (navigationView != null) {
            MenuItem item = navigationView.getMenu().findItem(id);
            if (item != null) {
                item.setVisible(visible);
            }
        }
    }

    private void loadDynamicAvatar(String name) {
        sideNavHeaderBinding.headerProfilePic.setVisibility(View.GONE);
        sideNavHeaderBinding.headerProfilePicDynamic.setVisibility(View.VISIBLE);

        String initialsUrl = "https://ui-avatars.com/api/?name=" + Uri.encode(name) + "&background=random&color=fff&size=256";
        if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
            Glide.with(MainActivity.this)
                    .load(initialsUrl)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(sideNavHeaderBinding.headerProfilePicDynamic);
        }
    }


    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = result.getData().getData();
                    Log.i("ImageURI", uri.getPath());

                    // We don't know yet if it's dynamic or real, but we usually show the selected one in real.
                    sideNavHeaderBinding.headerProfilePic.setVisibility(View.VISIBLE);
                    sideNavHeaderBinding.headerProfilePicDynamic.setVisibility(View.GONE);

                    if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
                        Glide.with(MainActivity.this)
                                .load(uri)
                                .circleCrop()
                                .into(sideNavHeaderBinding.headerProfilePic);
                    }


                    String imageId = UUID.randomUUID().toString();

                    FirebaseStorage storage = FirebaseStorage.getInstance();

                    StorageReference imageReference = storage.getReference("profile-images").child(imageId);
                    imageReference.putFile(uri)
                            .addOnSuccessListener(taskSnapshot -> {

                                firebaseFirestore.collection("users")
                                        .document(firebaseAuth.getUid())
                                        .update("profilePicUrl", imageId)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(MainActivity.this, "Profile image changed!", Toast.LENGTH_SHORT).show();
                                            updateSideNavHeader();
                                        });
                            });

                }
            }
    );


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.side_nav_home || itemId == R.id.bottom_nav_home) {
            loadFragment(new HomeFragment(), true);
        } else if (itemId == R.id.side_nav_profile || itemId == R.id.bottom_nav_profile) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
            } else {
                loadFragment(new ProfileFragment(), true);
            }
        } else if (itemId == R.id.side_nav_orders) {
            loadFragment(new OrdersFragment(), true);
        } else if (itemId == R.id.side_nav_wishlist) {
            loadFragment(new WishlistFragment(), true);
        } else if (itemId == R.id.side_nav_cart || itemId == R.id.bottom_nav_cart) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
            } else {
                loadFragment(new CartFragment(), true);
            }
        } else if (itemId == R.id.side_nav_nearby_shops) {
            loadFragment(new NearbyShopsFragment(), true);
        } else if (itemId == R.id.side_nav_message) {
            loadFragment(new MessageFragment(), true);
        } else if (itemId == R.id.side_nav_settings) {
            loadFragment(new SettingsFragment(), true);
        } else if (itemId == R.id.bottom_nav_category) {
            loadFragment(new CategoryFragment(), true);
        } else if (itemId == R.id.side_nav_talk_expert) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0772346088"));
            startActivity(intent);
        } else if (itemId == R.id.side_nav_login) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.side_nav_logout) {
            firebaseAuth.signOut();
            updateSideNavHeader();
            loadFragment(new HomeFragment(), true);
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
        
        bottomNavigationView.setVisibility(View.VISIBLE);
        updateBottomNavCheckedItem();
    }

    private void updateBottomNavCheckedItem() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof HomeFragment) {
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
            MenuItem homeItem = navigationView.getMenu().findItem(R.id.side_nav_home);
            if (homeItem != null) homeItem.setChecked(true);
        } else if (currentFragment instanceof CategoryFragment || currentFragment instanceof ListingFragment) {
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_category).setChecked(true);
        } else if (currentFragment instanceof CartFragment) {
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_cart).setChecked(true);
            MenuItem cartItem = navigationView.getMenu().findItem(R.id.side_nav_cart);
            if (cartItem != null) cartItem.setChecked(true);
        } else if (currentFragment instanceof ProfileFragment) {
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_profile).setChecked(true);
            MenuItem profileItem = navigationView.getMenu().findItem(R.id.side_nav_profile);
            if (profileItem != null) profileItem.setChecked(true);
        } else if (currentFragment instanceof NearbyShopsFragment) {
            MenuItem nearbyItem = navigationView.getMenu().findItem(R.id.side_nav_nearby_shops);
            if (nearbyItem != null) nearbyItem.setChecked(true);
        }
    }

    // Custom Color Spinner Adapter
    private class ColorSpinnerAdapter extends ArrayAdapter<String> {
        public ColorSpinnerAdapter(Context context, List<String> colors) {
            super(context, R.layout.item_spinner_color, R.id.text_color_name, colors);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createViewFromResource(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return createViewFromResource(position, convertView, parent);
        }

        private View createViewFromResource(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spinner_color, parent, false);
            }
            View colorView = convertView.findViewById(R.id.view_color_preview);
            TextView textView = convertView.findViewById(R.id.text_color_name);

            String colorCode = getItem(position);
            textView.setText(colorCode);

            if ("All Colors".equals(colorCode)) {
                colorView.setVisibility(View.GONE);
            } else {
                colorView.setVisibility(View.VISIBLE);
                try {
                    colorView.setBackgroundColor(Color.parseColor(colorCode));
                } catch (Exception e) {
                    colorView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            return convertView;
        }
    }
}
