package com.base.android.utils;

import android.view.MenuItem;
import androidx.databinding.BindingAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class BindingAdapters {
    @BindingAdapter("onItemSelected")
    public static void setOnItemSelected(BottomNavigationView view, NavigationBarView.OnItemSelectedListener listener) {
        view.setOnItemSelectedListener(listener);
    }
}