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
        SharedPrefManager.init(this);
        applyTheme();
    }

    private void applyTheme() {
        String theme = SharedPrefManager.getInstance().getTheme();
        switch (theme) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "transparent":
                // This is more complex and usually set on a per-activity basis.
                // For now, we'll treat it like light mode at the app level.
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default: // "light"
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        SharedPrefManager.init(base);
        Context newContext = LanguageManager.setInitialLocale(base);
        super.attachBaseContext(newContext);
    }
}
