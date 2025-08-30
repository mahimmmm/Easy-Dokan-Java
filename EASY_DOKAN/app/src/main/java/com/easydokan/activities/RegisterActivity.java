package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.easydokan.R;
import com.easydokan.databinding.ActivityRegisterBinding;
import com.easydokan.models.UserModel;
import com.easydokan.utils.LanguageManager;
import com.easydokan.utils.SharedPrefManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

        setupLanguageSwitch();
        setupClickListeners();
    }

    private void setupLanguageSwitch() {
        String currentLanguage = SharedPrefManager.getInstance().getLanguage();
        binding.languageSwitch.setChecked("bn".equals(currentLanguage));

        binding.languageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String lang = isChecked ? "bn" : "en";
            LanguageManager.updateLocale(this, lang);
            recreate();
        });
    }

    private void setupClickListeners() {
        binding.registerButton.setOnClickListener(v -> registerUser());
        binding.loginText.setOnClickListener(v ->
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        String name = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();
        String role = binding.radioShopOwner.isChecked() ? getString(R.string.role_shop_owner) : getString(R.string.role_staff);

        if (!validateInput(name, email, phone, password, confirmPassword)) {
            return;
        }

        // Show progress bar
        // binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            UserModel user = new UserModel(uid, name, email, phone, role);
                            saveUserToFirestore(user);
                        }
                    } else {
                        // binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(UserModel user) {
        db.collection("users").document(user.getUid())
                .set(user)
                .addOnCompleteListener(task -> {
                    // binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut(); // Sign out user to force login after registration
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to save user data: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInput(String name, String email, String phone, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            binding.nameLayout.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Valid email is required");
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            binding.phoneLayout.setError("Phone number is required");
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.passwordLayout.setError("Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordLayout.setError("Passwords do not match");
            return false;
        }
        // Clear errors if validation passes
        binding.nameLayout.setError(null);
        binding.emailLayout.setError(null);
        binding.phoneLayout.setError(null);
        binding.passwordLayout.setError(null);
        binding.confirmPasswordLayout.setError(null);
        return true;
    }
}
