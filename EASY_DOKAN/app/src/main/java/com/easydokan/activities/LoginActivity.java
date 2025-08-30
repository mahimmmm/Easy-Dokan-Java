package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.easydokan.databinding.ActivityLoginBinding;
import com.easydokan.utils.LanguageManager;
import com.easydokan.utils.SharedPrefManager;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

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
        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.registerText.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        // TODO: Add forgot password functionality
    }

    private void loginUser() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Password is required");
            return;
        }

        // Clear errors
        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);

        // Show progress bar (optional, but good for UX)
        // binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
