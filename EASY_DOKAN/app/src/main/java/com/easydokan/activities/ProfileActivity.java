package com.easydokan.activities;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.easydokan.databinding.ActivityProfileBinding;
import com.easydokan.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private DocumentReference userRef;
    private Uri imageUri;
    private ActivityResultLauncher<String> mGetContent;
    private String currentProfileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebase();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        loadUserProfile();
        registerImagePicker();

        binding.changePictureButton.setOnClickListener(v -> mGetContent.launch("image/*"));
        binding.saveProfileButton.setOnClickListener(v -> saveProfileChanges());
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
        if (mAuth.getCurrentUser() != null) {
            userRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
        }
    }

    private void registerImagePicker() {
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                imageUri = uri;
                binding.profileImage.setImageURI(imageUri);
            }
        });
    }

    private void loadUserProfile() {
        if (userRef != null) {
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    UserModel user = documentSnapshot.toObject(UserModel.class);
                    binding.nameEditText.setText(user.getName());
                    binding.emailEditText.setText(user.getEmail());
                    binding.phoneEditText.setText(user.getPhone());
                    binding.roleEditText.setText(user.getRole());
                    currentProfileImageUrl = user.getProfileImageUrl();
                    if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                        Glide.with(this).load(currentProfileImageUrl).into(binding.profileImage);
                    }
                }
            });
        }
    }

    private void saveProfileChanges() {
        if (imageUri != null) {
            uploadImageAndUpdateProfile();
        } else {
            updateProfileInfo(null);
        }
    }

    private void uploadImageAndUpdateProfile() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        StorageReference fileRef = storageRef.child("profile_pictures/" + mAuth.getCurrentUser().getUid());
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                progressDialog.dismiss();
                updateProfileInfo(uri.toString());
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateProfileInfo(String newImageUrl) {
        String name = binding.nameEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        if (newImageUrl != null) {
            updates.put("profileImageUrl", newImageUrl);
        }

        if (userRef != null) {
            userRef.update(updates).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                // Optionally delete old image if a new one was uploaded
                if (newImageUrl != null && currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                    FirebaseStorage.getInstance().getReferenceFromUrl(currentProfileImageUrl).delete();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
