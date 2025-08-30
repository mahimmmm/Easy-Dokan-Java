package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.easydokan.R;
import com.easydokan.databinding.ActivitySettingsBinding;
import com.easydokan.utils.LanguageManager;
import com.easydokan.utils.SharedPrefManager;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPrefManager = SharedPrefManager.getInstance();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupLanguageOptions();
        setupDarkModeSwitch();
        setupClickListeners();
    }

    private void setupLanguageOptions() {
        String currentLanguage = sharedPrefManager.getLanguage();
        if ("bn".equals(currentLanguage)) {
            binding.languageRadioGroup.check(R.id.radio_bangla);
        } else {
            binding.languageRadioGroup.check(R.id.radio_english);
        }

        binding.languageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = "en";
            if (checkedId == R.id.radio_bangla) {
                lang = "bn";
            }
            // Avoid recreating if the language is not changed
            if (!lang.equals(sharedPrefManager.getLanguage())) {
                LanguageManager.updateLocale(this, lang);
                recreate();
            }
        });
    }

    private void setupDarkModeSwitch() {
        binding.darkModeSwitch.setChecked(sharedPrefManager.isDarkModeEnabled());
        binding.darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPrefManager.saveDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setupClickListeners() {
        binding.profileCard.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        binding.logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
