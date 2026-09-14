package com.base.android.helper;

import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;
import com.base.android.data.local.prefs.PreferencesService;

public class ThemeHelper {

    public static void applyTheme(String themeMode) {
        if (themeMode == null) {
            themeMode = PreferencesService.THEME_MODE_DARK;
        }

        switch (themeMode) {
            case PreferencesService.THEME_MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case PreferencesService.THEME_MODE_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case PreferencesService.THEME_MODE_DARK:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    public static void applyThemeToConfiguration(Configuration config, String themeMode) {
        if (config == null) return;
        if (themeMode == null) {
            themeMode = PreferencesService.THEME_MODE_DARK;
        }

        switch (themeMode) {
            case PreferencesService.THEME_MODE_LIGHT:
                config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                        | Configuration.UI_MODE_NIGHT_NO;
                break;
            case PreferencesService.THEME_MODE_DARK:
                config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                        | Configuration.UI_MODE_NIGHT_YES;
                break;
            case PreferencesService.THEME_MODE_SYSTEM:
            default:
                // Leave uiMode as system determines
                break;
        }
    }
}
