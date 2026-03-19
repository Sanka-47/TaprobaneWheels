package lk.jiat.eshop.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import lk.jiat.eshop.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_REMEMBER_ME = "remember_me";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Shared Preferences Implementation
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Load saved values
        binding.switchNotifications.setChecked(prefs.getBoolean(KEY_NOTIFICATIONS, true));
        binding.switchRememberMe.setChecked(prefs.getBoolean(KEY_REMEMBER_ME, false));

        // Save on change
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
        });

        binding.switchRememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_REMEMBER_ME, isChecked).apply();
        });

        // 2. Internal Storage & Cache Management Implementation
        binding.btnClearCache.setOnClickListener(v -> {
            clearAppCache();
        });

        // Example of Internal Storage usage: saving a simple log file
        saveLastSettingsVisit();
    }

    private void saveLastSettingsVisit() {
        String filename = "settings_log.txt";
        String fileContents = "Last visited settings at: " + System.currentTimeMillis() + "\n";
        try (FileOutputStream fos = requireContext().openFileOutput(filename, Context.MODE_APPEND)) {
            fos.write(fileContents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearAppCache() {
        try {
            File cacheDir = requireContext().getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
                Toast.makeText(getContext(), "Local cache cleared successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to clear cache", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) return false;
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
