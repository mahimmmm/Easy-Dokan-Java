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
    }

    @Override
    protected void attachBaseContext(Context base) {
        SharedPrefManager.init(base);
        Context newContext = LanguageManager.setInitialLocale(base);
        super.attachBaseContext(newContext);
    }
}
