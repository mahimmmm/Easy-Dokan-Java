package com.easydokan.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LanguageManager {

    public static Context setInitialLocale(Context context) {
        String lang = SharedPrefManager.getInstance().getLanguage();
        return updateResources(context, lang);
    }

    public static Context updateLocale(Context context, String lang) {
        SharedPrefManager.getInstance().saveLanguage(lang);
        return updateResources(context, lang);
    }

    private static Context updateResources(Context context, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
        } else {
            config.locale = locale;
        }

        res.updateConfiguration(config, res.getDisplayMetrics());
        return context.createConfigurationContext(config);
    }
}
