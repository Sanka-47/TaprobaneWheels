package lk.jiat.eshop.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.adapter.SectionAdapter;
import lk.jiat.eshop.databinding.FragmentHomeBinding;
import lk.jiat.eshop.model.Product;

public class HomeFragment extends Fragment implements SensorEventListener {

    private FragmentHomeBinding binding;
    private SensorManager sensorManager;
    private float acceleration;
    private float currentAcceleration;
    private float lastAcceleration;
    private static final int SHAKE_THRESHOLD = 12;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        acceleration = 10f;
        currentAcceleration = SensorManager.GRAVITY_EARTH;
        lastAcceleration = SensorManager.GRAVITY_EARTH;

        setupIntroVideo();
        loadTopSellProduct();

    }

    private void setupIntroVideo() {
        Uri videoUri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.intro);
        binding.introVideoView.setVideoURI(videoUri);
        
        binding.introVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0, 0); // Mute the video
            float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
            float screenRatio = binding.introVideoView.getWidth() / (float) binding.introVideoView.getHeight();
            float scaleX = videoRatio / screenRatio;

            if (scaleX >= 1f) {
                binding.introVideoView.setScaleX(scaleX);
            } else {
                binding.introVideoView.setScaleY(1f / scaleX);
            }
            binding.introVideoView.start();
        });
        
        binding.introVideoContainer.setOnClickListener(v -> {
            if (binding.introVideoView.isPlaying()) {
                binding.introVideoView.pause();
            } else {
                binding.introVideoView.start();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (binding != null && !binding.introVideoView.isPlaying()) {
            binding.introVideoView.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (binding != null && binding.introVideoView.isPlaying()) {
            binding.introVideoView.pause();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;

        if (acceleration > SHAKE_THRESHOLD) {
            Toast.makeText(getContext(), "Refreshing products...", Toast.LENGTH_SHORT).show();
            loadTopSellProduct();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void loadTopSellProduct() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("products")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot qds) {
                        if (!qds.isEmpty()) {
                            List<Product> products = qds.toObjects(Product.class);


                            LinearLayoutManager layoutManager =
                                    new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);

                            binding.homeTopSellSection.itemSectionContainer.setLayoutManager(layoutManager);


                            SectionAdapter adapter = new SectionAdapter(products, product -> {
                                Bundle bundle = new Bundle();
                                bundle.putString("productId", product.getProductId());

                                ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                                productDetailsFragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, productDetailsFragment)
                                        .addToBackStack(null)
                                        .commit();
                            });

                            binding.homeTopSellSection.itemSectionTitle.setText("Top Selling Products");
                            binding.homeTopSellSection.itemSectionContainer.setAdapter(adapter);
                            binding.homeTopSellSection.itemSectionContainer.scheduleLayoutAnimation();

                        }
                    }
                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
