package lk.jiat.eshop.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.eshop.R;
import lk.jiat.eshop.databinding.FragmentNearbyShopsBinding;

public class NearbyShopsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "NearbyShopsFragment";
    private FragmentNearbyShopsBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private String productName;
    private String productImage;
    private LatLng userLocation;
    private Polyline polyline;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productName = getArguments().getString("productName");
            productImage = getArguments().getString("productImage");
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNearbyShopsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        if (productName != null) {
            binding.purchasedProductName.setText(productName);
        }
        if (productImage != null) {
             Glide.with(this).load(productImage).into(binding.purchasedProductImage);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f));

                    // Adding dummy tyre shops as examples (In real app, use Places API)
                    addDummyTyreShops(userLocation);
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (userLocation != null) {
                    getDirections(userLocation, marker.getPosition());
                }
                return false;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                LatLng position = marker.getPosition();
                String label = marker.getTitle();
                Uri gmmIntentUri = Uri.parse("geo:" + position.latitude + "," + position.longitude + "?q=" + Uri.encode(label));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });
    }

    private void getDirections(LatLng origin, LatLng destination) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();

        DirectionsApiRequest request = DirectionsApi.newRequest(context)
                .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.DRIVING);

        request.setCallback(new com.google.maps.PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                if (result.routes != null && result.routes.length > 0) {
                    List<com.google.maps.model.LatLng> path = result.routes[0].overviewPolyline.decodePath();
                    List<LatLng> newPath = new ArrayList<>();
                    for (com.google.maps.model.LatLng coords : path) {
                        newPath.add(new LatLng(coords.lat, coords.lng));
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (polyline != null) {
                            polyline.remove();
                        }
                        polyline = mMap.addPolyline(new PolylineOptions()
                                .addAll(newPath)
                                .color(Color.BLUE)
                                .width(10));
                        Log.d(TAG, "Directions fetched and polyline added");
                    });
                }
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "Failed to get directions", e);
                requireActivity().runOnUiThread(() -> 
                    Toast.makeText(getContext(), "Error fetching directions", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void addDummyTyreShops(LatLng currentLoc) {
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(currentLoc.latitude + 0.005, currentLoc.longitude + 0.005))
                .title("City Tyre House")
                .snippet("Specialized in Fitment")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(currentLoc.latitude - 0.008, currentLoc.longitude - 0.003))
                .title("Elite Wheel Alignment")
                .snippet("Professional alloy wheel fitment")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(currentLoc.latitude + 0.002, currentLoc.longitude - 0.006))
                .title("Auto Master Tyres")
                .snippet("Top rated service center")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}