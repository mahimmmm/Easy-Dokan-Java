package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easydokan.R;
import com.easydokan.databinding.ActivityRegisterBinding;
import com.easydokan.models.ProfileModel;
import com.easydokan.models.SettingsModel;
import com.easydokan.utils.LanguageManager;
import com.easydokan.utils.SharedPrefManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupToolbar();
        setupLanguageSwitch();
        setupClickListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupLanguageSwitch() {
        String currentLanguage = SharedPrefManager.getInstance().getLanguage();
        binding.languageSwitch.setChecked("bn".equals(currentLanguage));
        binding.languageSwitch.setText("bn".equals(currentLanguage) ? "EN" : "BN");

        binding.languageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String lang = isChecked ? "bn" : "en";
            LanguageManager.updateLocale(this, lang);
            recreate();
        });
    }

    // The role spinner is removed as it's not in the new DB plan.
    // The plan implies a single user type (the owner).
    private void setupClickListeners() {
        binding.registerButton.setOnClickListener(v -> registerUser());
        binding.backToLoginButton.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();

        if (!validateInput(name, email, phone, password, confirmPassword)) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            createInitialFirestoreData(firebaseUser, name, phone);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createInitialFirestoreData(FirebaseUser firebaseUser, String name, String phone) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();

        // Create default profile
        ProfileModel profile = new ProfileModel();
        profile.setName(name + "'s Store"); // Default store name
        profile.setOwner(name);
        profile.setEmail(email);
        profile.setPhone(phone);
        profile.setLanguage("en"); // Default language
        profile.setTheme("light"); // Default theme

        // Create default settings
        SettingsModel settings = new SettingsModel();
        settings.setLanguage("en");
        settings.setTheme("light");
        settings.setNotifications(true);
        settings.setBackup_enabled(true);

        // Use a batch write to save both documents atomically
        WriteBatch batch = db.batch();
        batch.set(db.collection("users").document(uid).collection("profile").document("user_profile"), profile);
        batch.set(db.collection("users").document(uid).collection("settings").document("user_settings"), settings);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
            // Navigate to the new Home Page
            Intent intent = new Intent(RegisterActivity.this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInput(String name, String email, String phone, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
