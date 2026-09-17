package com.base.android.helper;

import android.content.Context;
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

    public static boolean isDarkMode(Context context, String themeMode) {
        if (themeMode == null) {
            themeMode = PreferencesService.THEME_MODE_DARK;
        }
        if (PreferencesService.THEME_MODE_LIGHT.equals(themeMode)) {
            return false;
        } else if (PreferencesService.THEME_MODE_DARK.equals(themeMode)) {
            return true;
        } else {
            // SYSTEM
            if (context == null) return true;
            int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        }
    }

    // Theme Color Tokens (đồng bộ với values/colors.xml và values-night/colors.xml)
    public static int getScreenBackgroundColor(boolean isDark) {
        return isDark ? 0xFF182029 : 0xFFF0F2F5;
    }

    public static int getCardBackgroundColor(boolean isDark) {
        return isDark ? 0xFF090C0F : 0xFFFFFFFF;
    }

    public static int getNavBackgroundColor(boolean isDark) {
        return isDark ? 0xFF000000 : 0xFFFFFFFF;
    }

    public static int getBottomSheetBackgroundColor(boolean isDark) {
        return isDark ? 0xFF1F232B : 0xFFFFFFFF;
    }

    public static int getTextPrimaryColor(boolean isDark) {
        return isDark ? 0xFFFFFFFF : 0xFF111827;
    }

    public static int getTextSecondaryColor(boolean isDark) {
        return isDark ? 0xFFAEAEAE : 0xFF6B7280;
    }

    public static int getTextCancelColor(boolean isDark) {
        return isDark ? 0xFF9CA3AF : 0xFF6B7280;
    }

    public static int getDividerColor(boolean isDark) {
        return isDark ? 0xFF232D37 : 0xFFE5E7EB;
    }

    public static int getBottomSheetDividerColor(boolean isDark) {
        return isDark ? 0xFF2E3542 : 0xFFE5E7EB;
    }

    public interface ThemeRefreshable {
        void refreshTheme(boolean isDark);
    }
}
