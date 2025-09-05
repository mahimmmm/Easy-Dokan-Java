package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easydokan.R;
import com.easydokan.databinding.ActivityLoginBinding;
import com.easydokan.utils.LanguageManager;
import com.easydokan.utils.SharedPrefManager;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        sharedPrefManager = SharedPrefManager.getInstance();

        setupLanguageSwitch();
        setupClickListeners();
        checkRememberMe();
    }

    private void setupLanguageSwitch() {
        String currentLanguage = sharedPrefManager.getLanguage();
        binding.languageSwitch.setChecked("bn".equals(currentLanguage));
        binding.languageSwitch.setText("bn".equals(currentLanguage) ? "EN" : "BN");


        binding.languageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String lang = isChecked ? "bn" : "en";
            LanguageManager.updateLocale(this, lang);
            recreate();
        });
    }

    private void checkRememberMe() {
        if (sharedPrefManager.isRememberMeChecked()) {
            binding.emailEditText.setText(sharedPrefManager.getSavedEmail());
            binding.rememberMeCheckbox.setChecked(true);
        }
    }

    private void setupClickListeners() {
        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (binding.rememberMeCheckbox.isChecked()) {
                            sharedPrefManager.setRememberMe(true, email);
                        } else {
                            sharedPrefManager.setRememberMe(false, null);
                        }
                        Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
