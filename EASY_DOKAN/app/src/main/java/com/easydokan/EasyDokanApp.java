package com.easydokan;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.easydokan.utils.LanguageManager;
import com.easydokan.utils.SharedPrefManager;

public class EasyDokanApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // It's good practice to init here for the rest of the app lifecycle.
        SharedPrefManager.init(this);

        // Apply dark mode setting on startup
        if (SharedPrefManager.getInstance().isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        // Init SharedPreferences before using it to set the locale.
        SharedPrefManager.init(base);

        // Set initial locale and get the new context.
        Context newContext = LanguageManager.setInitialLocale(base);
        super.attachBaseContext(newContext);
    }
}
