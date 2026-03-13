package lk.jiat.eshop.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
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

import java.util.UUID;

import lk.jiat.eshop.R;
import lk.jiat.eshop.databinding.ActivityMainBinding;
import lk.jiat.eshop.databinding.SideNavHeaderBinding;
import lk.jiat.eshop.fragment.CartFragment;
import lk.jiat.eshop.fragment.CategoryFragment;
import lk.jiat.eshop.fragment.HomeFragment;
import lk.jiat.eshop.fragment.ListingFragment;
import lk.jiat.eshop.fragment.MessageFragment;
import lk.jiat.eshop.fragment.OrdersFragment;
import lk.jiat.eshop.fragment.ProductDetailsFragment;
import lk.jiat.eshop.fragment.ProfileFragment;
import lk.jiat.eshop.fragment.SearchFragment;
import lk.jiat.eshop.fragment.SettingsFragment;
import lk.jiat.eshop.fragment.WishlistFragment;
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
                    performSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        Bundle bundle = new Bundle();
        bundle.putString("query", query);
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
            sideNavHeaderBinding.headerProfilePic.setImageResource(R.drawable.person_24);

            firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            if (user != null) {
                                sideNavHeaderBinding.headerUserName.setText(user.getName());
                                sideNavHeaderBinding.headerUserEmail.setText(user.getEmail());

                                if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isEmpty()) {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    storage.getReference("profile-images/" + user.getProfilePicUrl()).getDownloadUrl()
                                            .addOnSuccessListener(uri -> {
                                                Glide.with(MainActivity.this)
                                                        .load(uri)
                                                        .circleCrop()
                                                        .into(sideNavHeaderBinding.headerProfilePic);
                                            });
                                }
                            }
                        } else {
                            Log.e("Firestore", "Document does not exist for UID: " + currentUser.getUid());
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("Firestore", "Error: " + e.getMessage());
                    });

            // Update Menu Visibility
            navigationView.getMenu().findItem(R.id.side_nav_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.side_nav_profile).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_orders).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_wishlist).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_cart).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_message).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_logout).setVisible(true);

            sideNavHeaderBinding.headerProfilePic.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);
            });
        } else {
            sideNavHeaderBinding.headerUserName.setText("You're not logged in");
            sideNavHeaderBinding.headerUserEmail.setText("");
            sideNavHeaderBinding.headerProfilePic.setImageResource(R.drawable.person_24);
            
            navigationView.getMenu().findItem(R.id.side_nav_login).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_profile).setVisible(false);
            navigationView.getMenu().findItem(R.id.side_nav_orders).setVisible(false);
            navigationView.getMenu().findItem(R.id.side_nav_wishlist).setVisible(false);
            navigationView.getMenu().findItem(R.id.side_nav_cart).setVisible(false);
            navigationView.getMenu().findItem(R.id.side_nav_message).setVisible(false);
            navigationView.getMenu().findItem(R.id.side_nav_logout).setVisible(false);
        }
    }


    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri uri = result.getData().getData();
                    Log.i("ImageURI", uri.getPath());

                    Glide.with(MainActivity.this)
                            .load(uri)
                            .circleCrop()
                            .into(sideNavHeaderBinding.headerProfilePic);


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
            navigationView.getMenu().findItem(R.id.side_nav_home).setChecked(true);
        } else if (currentFragment instanceof CategoryFragment || currentFragment instanceof ListingFragment) {
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_category).setChecked(true);
        } else if (currentFragment instanceof CartFragment) {
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_cart).setChecked(true);
            navigationView.getMenu().findItem(R.id.side_nav_cart).setChecked(true);
        } else if (currentFragment instanceof ProfileFragment) {
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_profile).setChecked(true);
            navigationView.getMenu().findItem(R.id.side_nav_profile).setChecked(true);
        }
    }
}
