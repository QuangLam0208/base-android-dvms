package com.base.android.ui.main;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import com.base.android.ui.base.fragment.BaseFragment;
import com.base.android.ui.main.account.login.LoginActivity;
import com.base.android.ui.main.account.profile.ProfileFragment;
import com.base.android.ui.main.chart.ChartFragment;
import com.base.android.ui.main.company.CompanyFragment;
import com.base.android.ui.main.courses.CoursesFragment;
import com.base.android.ui.main.mentor.MentorFragment;
import com.base.android.ui.main.reviews.ReviewsFragment;

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
            transaction.hide(activeFragment); // ẩn tab cũ
        }

        Fragment targetFragment = fm.findFragmentByTag(targetTag);

        if (targetFragment == null) {
            targetFragment = createFragmentByTag(targetTag);
            transaction.add(R.id.fragment_container, targetFragment, targetTag);
        } else {
            transaction.show(targetFragment); // hiện tab mới -> kích hoạt onHiddenChanged(false)
        }
        transaction.commit();

        activeFragment = targetFragment;
        currentTag = targetTag;
    }

    public void applyThemeColors(boolean isDark) {
        viewModel.setNightMode(isDark);

        int screenBg = ThemeHelper.getScreenBackgroundColor(isDark);
        int navBg = ThemeHelper.getNavBackgroundColor(isDark);

        // Đổi màu Status bar & Navigation bar hệ thống
        getWindow().setStatusBarColor(screenBg);
        getWindow().setNavigationBarColor(navBg);

        // Đổi màu icon pin, sóng, đồng hồ trên Status bar (Đen khi nền sáng, Trắng khi nền tối)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (controller != null) {
                controller.setAppearanceLightStatusBars(!isDark);
                controller.setAppearanceLightNavigationBars(!isDark);
            }
        }
    }

    /**
     * Hệ điều hành Android tự động gọi khi máy đổi Dark/Light mode ở chế độ hệ thống.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String currentTheme = viewModel.getTheme();
        if (PreferencesService.THEME_MODE_SYSTEM.equals(currentTheme)) {
            // Lấy cờ night mode trực tiếp từ cấu hình hệ thống vừa truyền vào
            boolean isDark = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            ThemeHelper.setSystemNightMode(isDark); // Cập nhật cache
            applyThemeColors(isDark); // Đổi màu Status Bar, Nav Bar
            if (activeFragment instanceof BaseFragment) {
                ((BaseFragment<?, ?>) activeFragment).checkAndSyncNightMode(); // Đổi màu tab đang mở
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
        String currentTheme = viewModel.getTheme();
        if (PreferencesService.THEME_MODE_SYSTEM.equals(currentTheme)) {
            int systemMode = Resources.getSystem().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (systemMode != Configuration.UI_MODE_NIGHT_UNDEFINED) {
                ThemeHelper.setSystemNightMode(systemMode == Configuration.UI_MODE_NIGHT_YES);
            }
            boolean isDark = ThemeHelper.isDarkMode(this, currentTheme);
            if (viewModel.isNightMode.get() != isDark) {
                applyThemeColors(isDark);
            }
            if (activeFragment instanceof BaseFragment) {
                ((BaseFragment<?, ?>) activeFragment).checkAndSyncNightMode();
            }
        }
    }
    @Override
    public void doExpireSession() {
        Intent intent = new Intent(this, LoginActivity.class);
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
