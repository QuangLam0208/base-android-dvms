package com.base.android.helper;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    public static final String LANGUAGE_VI = "vi";
    public static final String LANGUAGE_EN = "en";

    public static Context setLocale(Context context, String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            languageCode = LANGUAGE_VI;
        }

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLayoutDirection(locale);
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                config.setLayoutDirection(locale);
            }
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            return context;
        }
    }
}
