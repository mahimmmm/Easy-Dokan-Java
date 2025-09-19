package com.easydokan.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.easydokan.R;
import com.easydokan.databinding.ActivitySettingsBinding;
import com.easydokan.models.ProfileModel;
import com.easydokan.models.SettingsModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;
    private DocumentReference profileRef;
    private DocumentReference settingsRef;
    private ProfileModel currentProfile;
    private SettingsModel currentSettings;
    private ActivityResultLauncher<String> mGetContent;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebase();
        setupToolbar();
        registerImagePicker();
        setupClickListeners();
        loadSettingsAndProfile();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
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

    private void registerImagePicker() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imageUri = uri;
                uploadProfilePicture();
            }
        });
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
            if (currentProfile.getImageUrl() != null && !currentProfile.getImageUrl().isEmpty()) {
                Glide.with(this).load(currentProfile.getImageUrl()).placeholder(R.drawable.ic_person_blue).into(binding.profileImage);
            }
        }
    }

    private void updateSettingsUI() {
        if (currentSettings != null) {
            binding.switchNotifications.setChecked(currentSettings.isNotifications());
        }
    }

    private void setupClickListeners() {
        binding.profileImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        binding.editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        binding.itemLanguage.setOnClickListener(v -> showToast("Language change coming soon!"));
        binding.itemTheme.setOnClickListener(v -> showToast("Theme change coming soon!"));
        binding.itemChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentSettings != null && settingsRef != null) {
                settingsRef.update("notifications", isChecked);
            }
        });
        binding.itemSupport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@easydokan.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request for Easy Dokan");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.itemDevInfo.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Developer Information")
                .setMessage("App developed by ASLAM.")
                .setPositiveButton("OK", null)
                .show();
        });

        binding.logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void uploadProfilePicture() {
        if (imageUri == null || mAuth.getCurrentUser() == null) return;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image...");
        progressDialog.show();

        StorageReference fileRef = storageRef.child("profile_images/" + mAuth.getCurrentUser().getUid());

        fileRef.putFile(imageUri)
            .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                updateProfileImageUrl(uri.toString());
                progressDialog.dismiss();
            }))
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
    }

    private void updateProfileImageUrl(String url) {
        if (profileRef != null) {
            profileRef.update("imageUrl", url)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    loadSettingsAndProfile(); // Refresh UI
                });
        }
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        final EditText currentPassEt = dialogView.findViewById(R.id.current_password_edit_text);
        final EditText newPassEt = dialogView.findViewById(R.id.new_password_edit_text);
        final EditText confirmPassEt = dialogView.findViewById(R.id.confirm_new_password_edit_text);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String currentPass = currentPassEt.getText().toString();
            String newPass = newPassEt.getText().toString();
            String confirmPass = confirmPassEt.getText().toString();

            if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(this, "New password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
                return;
            }
            handleChangePassword(currentPass, newPass);
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void handleChangePassword(String currentPassword, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Changing Password...");
        progressDialog.show();

        // Re-authenticate user first
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // If re-authentication is successful, update the password
                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                        progressDialog.dismiss();
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Authentication failed. Please check your current password.", Toast.LENGTH_LONG).show();
                }
            });
    }
}
