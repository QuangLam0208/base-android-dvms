package com.base.android.ui.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.base.android.BR;
import com.base.android.MVVMApplication;
import com.base.android.R;
import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.databinding.ActivityMainBinding;
import com.base.android.di.component.ActivityComponent;
import com.base.android.helper.ThemeHelper;
import com.base.android.ui.base.activity.BaseActivity;
import com.base.android.ui.main.account.profile.ProfileFragment;
import com.base.android.ui.main.chart.ChartFragment;
import com.base.android.ui.main.company.CompanyFragment;
import com.base.android.ui.main.courses.CoursesFragment;
import com.base.android.ui.main.mentor.MentorFragment;
import com.base.android.ui.main.reviews.ReviewsFragment;

import java.util.HashSet;
import java.util.Set;


public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> {

    public static final String TAG_COURSES = "TAG_COURSES";
    public static final String TAG_REVIEWS = "TAG_REVIEWS";
    public static final String TAG_CHARTS = "TAG_CHARTS";
    public static final String TAG_MENTOR = "TAG_MENTOR";
    public static final String TAG_COMPANY = "TAG_COMPANY";
    public static final String TAG_PROFILE = "TAG_PROFILE";
    public static final String KEY_CURRENT_TAG = "KEY_CURRENT_TAG";

    private String currentTag = TAG_COURSES;
    private Fragment activeFragment;
    private final Set<String> outdatedTabs = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        boolean isDark = ThemeHelper.isDarkMode(this, viewModel.getTheme());
        applyThemeColors(isDark);

        setupInitialFragment(savedInstanceState);

        // Setup Push Subscription Observer for verification dialog
        ((MVVMApplication) getApplication()).getAppComponent()
                .getOneSignalManager()
                .setupPushSubscriptionObserver(this);
    }

    private void setupInitialFragment (@Nullable Bundle savedInstanceState) {
        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            String initialTag = getIntent().getStringExtra(KEY_CURRENT_TAG);
            if (initialTag != null && !initialTag.isEmpty()) {
                currentTag = initialTag;
                activeFragment = createFragmentByTag(currentTag);
                fm.beginTransaction()
                        .add(R.id.fragment_container, activeFragment, currentTag)
                        .commit();
                int bottomNavItemId = getBottomNavItemIdByTag(currentTag);
                if (bottomNavItemId != 0 && bottomNavItemId != R.id.tab_courses) {
                    viewBinding.bottomNavigation.setSelectedItemId(bottomNavItemId);
                }
            } else {
                currentTag = TAG_COURSES;
                activeFragment = CoursesFragment.newInstance();
                fm.beginTransaction()
                        .add(R.id.fragment_container, activeFragment, TAG_COURSES)
                        .commit();
            }
        } else {
            currentTag = savedInstanceState.getString(KEY_CURRENT_TAG, TAG_COURSES);
            activeFragment = fm.findFragmentByTag(currentTag);
            if (activeFragment == null) {
                activeFragment = fm.findFragmentByTag(TAG_COURSES);
            }
        }
    }

    private int getBottomNavItemIdByTag(String tag) {
        switch (tag) {
            case TAG_CHARTS:
                return R.id.tab_charts;
            case TAG_MENTOR:
                return R.id.tab_mentor;
            case TAG_COMPANY:
                return R.id.tab_company;
            case TAG_PROFILE:
                return R.id.tab_profile;
            case TAG_COURSES:
            default:
                return R.id.tab_courses;
        }
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.tab_courses) {
            switchTab(TAG_COURSES);
            return true;
        } else if (itemId == R.id.tab_charts) {
            switchTab(TAG_CHARTS);
            return true;
        } else if (itemId == R.id.tab_mentor) {
            switchTab(TAG_MENTOR);
            return true;
        } else if (itemId == R.id.tab_company) {
            switchTab(TAG_COMPANY);
            return true;
        } else if (itemId == R.id.tab_profile) {
            switchTab(TAG_PROFILE);
            return true;
        }
        return false;
    }

    private void switchTab(String targetTag) {
        if (targetTag.equals(currentTag) && activeFragment != null) {
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        if (activeFragment != null) {
            transaction.hide(activeFragment);
        }

        Fragment targetFragment = fm.findFragmentByTag(targetTag);

        if (targetFragment == null) {
            targetFragment = createFragmentByTag(targetTag);
            transaction.add(R.id.fragment_container, targetFragment, targetTag);
        } else {
            transaction.show(targetFragment);
        }
        transaction.commit();

        if (outdatedTabs.contains(targetTag)) {
            outdatedTabs.remove(targetTag);
            boolean isDark = ThemeHelper.isDarkMode(this, viewModel.getTheme());
            if (targetFragment instanceof ThemeHelper.ThemeRefreshable) {
                ((ThemeHelper.ThemeRefreshable) targetFragment).refreshTheme(isDark);
            }
        }

        activeFragment = targetFragment;
        currentTag = targetTag;
    }

    public void applyThemeColors(boolean isDark) {
        int screenBg = ThemeHelper.getScreenBackgroundColor(isDark);
        int navBg = ThemeHelper.getNavBackgroundColor(isDark);

        // Status Bar & Navigation Bar colors
        getWindow().setStatusBarColor(screenBg);
        getWindow().setNavigationBarColor(navBg);

        // Light/Dark System Bar Icons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (controller != null) {
                controller.setAppearanceLightStatusBars(!isDark);
                controller.setAppearanceLightNavigationBars(!isDark);
            }
        }

        // Bottom Navigation background
        if (viewBinding != null && viewBinding.bottomNavigation != null) {
            viewBinding.bottomNavigation.setBackgroundColor(navBg);
        }
    }

    public void notifyThemeChanged() {
        outdatedTabs.clear();
        outdatedTabs.add(TAG_COURSES);
        outdatedTabs.add(TAG_REVIEWS);
        outdatedTabs.add(TAG_CHARTS);
        outdatedTabs.add(TAG_MENTOR);
        outdatedTabs.add(TAG_COMPANY);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String currentTheme = viewModel.getTheme();
        if (PreferencesService.THEME_MODE_SYSTEM.equals(currentTheme)) {
            boolean isDark = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            applyThemeColors(isDark);
            notifyThemeChanged();
            Fragment profileFrag = getSupportFragmentManager().findFragmentByTag(TAG_PROFILE);
            if (profileFrag instanceof ProfileFragment) {
                ((ProfileFragment) profileFrag).applyProfileThemeColors(isDark);
                ((ProfileFragment) profileFrag).updateThemeDisplay();
            }
        }
    }

    private Fragment createFragmentByTag(String tag) {
        switch (tag) {
            case TAG_CHARTS:
                return ChartFragment.newInstance();
            case TAG_REVIEWS:
                return ReviewsFragment.newInstance();
            case TAG_MENTOR:
                return MentorFragment.newInstance();
            case TAG_COMPANY:
                return CompanyFragment.newInstance();
            case TAG_PROFILE:
                return ProfileFragment.newInstance();
            case TAG_COURSES:
            default:
                return CoursesFragment.newInstance();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_TAG, currentTag);
    }

    @Override
    public void onBackPressed() {
        if (!TAG_COURSES.equals(currentTag)) {
            viewBinding.bottomNavigation.setSelectedItemId(R.id.tab_courses);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    public void doExpireSession() {
        Intent intent = new Intent(this, com.base.android.ui.main.account.login.LoginActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }
    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }
}
