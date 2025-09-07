package com.easydokan.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

        setupToolbar();
        setupClickListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        binding.getRoot().findViewById(R.id.item_language).setOnClickListener(v -> showLanguageDialog());
        binding.getRoot().findViewById(R.id.item_theme).setOnClickListener(v -> showThemeDialog());

        binding.logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Placeholder toasts for complex features
        binding.getRoot().findViewById(R.id.edit_profile_button).setOnClickListener(v -> showToast("Edit Profile coming soon!"));
        binding.getRoot().findViewById(R.id.item_change_password).setOnClickListener(v -> showToast("Change Password coming soon!"));
        binding.getRoot().findViewById(R.id.item_backup).setOnClickListener(v -> showToast("Backup Data coming soon!"));
        binding.getRoot().findViewById(R.id.item_export).setOnClickListener(v -> showToast("Export Data coming soon!"));
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "বাংলা"};
        int currentLangIndex = sharedPrefManager.getLanguage().equals("bn") ? 1 : 0;

        new AlertDialog.Builder(this)
            .setTitle(R.string.settings_item_language)
            .setSingleChoiceItems(languages, currentLangIndex, (dialog, which) -> {
                String lang = (which == 1) ? "bn" : "en";
                if (!lang.equals(sharedPrefManager.getLanguage())) {
                    sharedPrefManager.saveLanguage(lang);
                    LanguageManager.updateLocale(this, lang);
                    recreate();
                }
                dialog.dismiss();
            }).show();
    }

    private void showThemeDialog() {
        String[] themes = {getString(R.string.theme_light), getString(R.string.theme_dark), getString(R.string.theme_transparent)};
        String currentTheme = sharedPrefManager.getTheme();
        int currentThemeIndex = 0;
        if(currentTheme.equals("dark")) currentThemeIndex = 1;
        else if (currentTheme.equals("transparent")) currentThemeIndex = 2;

        new AlertDialog.Builder(this)
            .setTitle(R.string.settings_item_theme)
            .setSingleChoiceItems(themes, currentThemeIndex, (dialog, which) -> {
                String selectedTheme = "light";
                if (which == 1) selectedTheme = "dark";
                else if (which == 2) selectedTheme = "transparent";

                sharedPrefManager.saveTheme(selectedTheme);
                applyTheme(selectedTheme);
                dialog.dismiss();
            }).show();
    }

    private void applyTheme(String theme) {
        switch (theme) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "transparent":
                // This would require setting the theme on the activity before setContentView
                // For a live change, recreating the activity is best
                recreate();
                break;
            default: // "light"
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
