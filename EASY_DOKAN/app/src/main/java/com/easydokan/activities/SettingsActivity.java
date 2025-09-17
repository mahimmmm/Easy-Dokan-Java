package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.easydokan.R;
import com.easydokan.databinding.ActivitySettingsBinding;
import com.easydokan.models.ProfileModel;
import com.easydokan.models.SettingsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DocumentReference profileRef;
    private DocumentReference settingsRef;
    private ProfileModel currentProfile;
    private SettingsModel currentSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebase();
        setupToolbar();
        setupClickListeners();
        loadSettingsAndProfile();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            profileRef = db.collection("users").document(uid).collection("profile").document("user_profile");
            settingsRef = db.collection("users").document(uid).collection("settings").document("user_settings");
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadSettingsAndProfile() {
        if (profileRef != null) {
            profileRef.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    currentProfile = doc.toObject(ProfileModel.class);
                    updateProfileUI();
                }
            });
        }
        if (settingsRef != null) {
            settingsRef.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    currentSettings = doc.toObject(SettingsModel.class);
                    updateSettingsUI();
                }
            });
        }
    }

    private void updateProfileUI() {
        if (currentProfile != null) {
            binding.storeNameText.setText(currentProfile.getName());
            binding.ownerNameText.setText(currentProfile.getOwner());
            binding.profileEmailText.setText(currentProfile.getEmail());
            binding.profilePhoneText.setText(currentProfile.getPhone());
        }
    }

    private void updateSettingsUI() {
        if (currentSettings != null) {
            binding.switchNotifications.setChecked(currentSettings.isNotifications());
        }
    }

    private void setupClickListeners() {
        binding.editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        binding.itemLanguage.setOnClickListener(v -> showLanguageDialog());
        binding.itemTheme.setOnClickListener(v -> showThemeDialog());
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null && settingsRef != null) {
                settingsRef.update("notifications", isChecked);
            }
        });
        binding.logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showEditProfileDialog() {
        if (currentProfile == null) {
            Toast.makeText(this, "Profile data not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        final EditText storeNameEt = dialogView.findViewById(R.id.store_name_edit_text);
        final EditText ownerNameEt = dialogView.findViewById(R.id.owner_name_edit_text);
        final EditText phoneEt = dialogView.findViewById(R.id.phone_edit_text);

        storeNameEt.setText(currentProfile.getName());
        ownerNameEt.setText(currentProfile.getOwner());
        phoneEt.setText(currentProfile.getPhone());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String storeName = storeNameEt.getText().toString().trim();
            String ownerName = ownerNameEt.getText().toString().trim();
            String phone = phoneEt.getText().toString().trim();
            if (TextUtils.isEmpty(storeName) || TextUtils.isEmpty(ownerName)) {
                Toast.makeText(this, "Store and Owner name cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            updateProfileInFirestore(storeName, ownerName, phone);
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void updateProfileInFirestore(String storeName, String ownerName, String phone) {
        if (profileRef != null) {
            profileRef.update("name", storeName, "owner", ownerName, "phone", phone)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    loadSettingsAndProfile(); // Refresh UI
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show());
        }
    }

    private void showLanguageDialog() {
        // ... (Implementation would be similar, but updates Firestore and then recreates)
        Toast.makeText(this, "Language change feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showThemeDialog() {
        // ... (Implementation would be similar, but updates Firestore and then applies theme)
        Toast.makeText(this, "Theme change feature coming soon!", Toast.LENGTH_SHORT).show();
    }
}
