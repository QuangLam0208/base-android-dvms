package com.base.android.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.content.res.Resources;
import androidx.appcompat.app.AppCompatDelegate;
import com.base.android.constant.Constants;
import com.base.android.data.local.prefs.PreferencesService;

public class ThemeHelper {

    private static Boolean sSystemNightMode = null;

    public static void setSystemNightMode(boolean isDark) {
        sSystemNightMode = isDark;
    }

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

    public static boolean isDarkMode(Context context) {
        if (context == null) return true;
        try {
            SharedPreferences sp = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
            String themeMode = sp.getString(PreferencesService.KEY_THEME_MODE, PreferencesService.THEME_MODE_DARK);
            return isDarkMode(context, themeMode);
        } catch (Exception e) {
            return true;
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
            // SYSTEM: Ưu tiên cache từ onConfigurationChanged, sau đó kiểm tra Resources.getSystem()
            if (sSystemNightMode != null) {
                return sSystemNightMode;
            }
            // Đọc trực tiếp từ Configuration cấp hệ thống (Framework-level)
            int systemMode = Resources.getSystem().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (systemMode == Configuration.UI_MODE_NIGHT_YES) {
                return true;
            } else if (systemMode == Configuration.UI_MODE_NIGHT_NO) {
                return false;
            }
            // Fallback cuối cùng qua context
            if (context != null) {
                Context appCtx = context.getApplicationContext();
                int appMode = (appCtx != null ? appCtx : context).getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                return appMode == Configuration.UI_MODE_NIGHT_YES;
            }
            return false;
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
}
