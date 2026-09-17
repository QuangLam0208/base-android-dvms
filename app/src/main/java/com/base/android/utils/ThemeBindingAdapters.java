package com.base.android.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ThemeBindingAdapters {

    private static Context getThemedContext(Context context, Boolean isNightMode) {
        if (context == null) return null;
        boolean isDark = isNightMode != null && isNightMode;
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                | (isDark ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO);
        return context.createConfigurationContext(config);
    }

    @BindingAdapter(value = {"bgColorResByMode", "isNightMode"}, requireAll = false)
    public static void setBgColorResByMode(View view, Integer colorResId, Boolean isNightMode) {
        if (view == null || colorResId == null || colorResId == 0) return;
        Context themedContext = getThemedContext(view.getContext(), isNightMode);
        if (themedContext != null) {
            int color = ContextCompat.getColor(themedContext, colorResId);
            view.setBackgroundColor(color);
        }
    }

    @BindingAdapter(value = {"cardBgColorResByMode", "isNightMode"}, requireAll = false)
    public static void setCardBgColorResByMode(MaterialCardView cardView, Integer colorResId, Boolean isNightMode) {
        if (cardView == null || colorResId == null || colorResId == 0) return;
        Context themedContext = getThemedContext(cardView.getContext(), isNightMode);
        if (themedContext != null) {
            int color = ContextCompat.getColor(themedContext, colorResId);
            cardView.setCardBackgroundColor(color);
        }
    }

    @BindingAdapter(value = {"textColorResByMode", "isNightMode"}, requireAll = false)
    public static void setTextColorResByMode(TextView textView, Integer colorResId, Boolean isNightMode) {
        if (textView == null || colorResId == null || colorResId == 0) return;
        Context themedContext = getThemedContext(textView.getContext(), isNightMode);
        if (themedContext != null) {
            int color = ContextCompat.getColor(themedContext, colorResId);
            textView.setTextColor(color);
        }
    }

    @BindingAdapter(value = {"tintColorResByMode", "isNightMode"}, requireAll = false)
    public static void setTintColorResByMode(ImageView imageView, Integer colorResId, Boolean isNightMode) {
        if (imageView == null || colorResId == null || colorResId == 0) return;
        Context themedContext = getThemedContext(imageView.getContext(), isNightMode);
        if (themedContext != null) {
            int color = ContextCompat.getColor(themedContext, colorResId);
            imageView.setImageTintList(ColorStateList.valueOf(color));
        }
    }

    @BindingAdapter(value = {"strokeColorResByMode", "isNightMode"}, requireAll = false)
    public static void setStrokeColorResByMode(MaterialCardView cardView, Integer colorResId, Boolean isNightMode) {
        if (cardView == null || colorResId == null || colorResId == 0) return;
        Context themedContext = getThemedContext(cardView.getContext(), isNightMode);
        if (themedContext != null) {
            int color = ContextCompat.getColor(themedContext, colorResId);
            cardView.setStrokeColor(color);
        }
    }

    @BindingAdapter(value = {"bgTintResByMode", "isNightMode"}, requireAll = false)
    public static void setBgTintResByMode(View view, Integer colorResId, Boolean isNightMode) {
        if (view == null || colorResId == null || colorResId == 0) return;
        Context themedContext = getThemedContext(view.getContext(), isNightMode);
        if (themedContext != null) {
            int color = ContextCompat.getColor(themedContext, colorResId);
            view.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    @BindingAdapter(value = {"bgDrawableResByMode", "isNightMode"}, requireAll = false)
    public static void setBgDrawableResByMode(View view, Integer drawableResId, Boolean isNightMode) {
        if (view == null || drawableResId == null || drawableResId == 0) return;
        Context themedContext = getThemedContext(view.getContext(), isNightMode);
        if (themedContext != null) {
            view.setBackground(ContextCompat.getDrawable(themedContext, drawableResId));
        }
    }

    @BindingAdapter(value = {"buttonIconTintResByMode", "isNightMode"}, requireAll = false)
    public static void setButtonIconTintResByMode(MaterialButton button, Integer colorResId, Boolean isNightMode) {
        if (button == null || colorResId == null || colorResId == 0) return;
        Context themedContext = getThemedContext(button.getContext(), isNightMode);
        if (themedContext != null) {
            int color = ContextCompat.getColor(themedContext, colorResId);
            button.setIconTint(ColorStateList.valueOf(color));
        }
    }
}
