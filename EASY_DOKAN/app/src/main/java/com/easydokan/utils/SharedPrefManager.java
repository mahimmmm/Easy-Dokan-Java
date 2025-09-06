package com.easydokan.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    private static final String SHARED_PREF_NAME = "EasyDokanPrefs";
    private static final String KEY_LANGUAGE = "key_language";
    private static final String KEY_REMEMBER_ME = "key_remember_me";
    private static final String KEY_SAVED_EMAIL = "key_saved_email";
    private static final String KEY_THEME = "key_theme";

    private static SharedPrefManager instance;
    private static SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
    }

    public static synchronized SharedPrefManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SharedPrefManager is not initialized, call init() first");
        }
        return instance;
    }

    // --- Language ---
    public void saveLanguage(String language) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, "en"); // Default to English
    }

    // --- Remember Me ---
    public void setRememberMe(boolean remember, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMEMBER_ME, remember);
        if (remember) {
            editor.putString(KEY_SAVED_EMAIL, email);
        } else {
            editor.remove(KEY_SAVED_EMAIL);
        }
        editor.apply();
    }

    public boolean isRememberMeChecked() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getSavedEmail() {
        return sharedPreferences.getString(KEY_SAVED_EMAIL, "");
    }

    // --- Theme ---
    public void saveTheme(String theme) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_THEME, theme);
        editor.apply();
    }

    public String getTheme() {
        return sharedPreferences.getString(KEY_THEME, "light"); // Default to light
    }
}
