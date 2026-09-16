package com.base.android.ui.main.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.base.android.BR;
import com.base.android.R;
import com.base.android.databinding.ActivitySplashBinding;
import com.base.android.di.component.ActivityComponent;
import com.base.android.ui.base.activity.BaseActivity;
import com.base.android.ui.main.MainActivity;
import com.base.android.ui.main.MainCallback;
import com.base.android.ui.main.account.login.LoginActivity;
import com.base.android.utils.PermissionUtils;

public class SplashActivity extends BaseActivity<ActivitySplashBinding, SplashViewModel> {

    private static final long SPLASH_DELAY = 1200L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean pendingNavigateToMain = false;

    // Launcher xin tất cả các quyền một thể khi mở app
    private final ActivityResultLauncher<String[]> multiplePermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // Người dùng đã tương tác xong với popup xin quyền
                proceedNavigation();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        startSplashCheck();
        startSpinnerAnimation();
    }

    private void startSpinnerAnimation() {
        android.view.animation.RotateAnimation rotate = new android.view.animation.RotateAnimation(
                0f, 360f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(900);
        rotate.setRepeatCount(android.view.animation.Animation.INFINITE);
        rotate.setInterpolator(new android.view.animation.LinearInterpolator());
        viewBinding.ivSplashSpinner.startAnimation(rotate);
    }

    private void startSplashCheck() {
        long startTime = System.currentTimeMillis();

        viewModel.checkAuthentication(new MainCallback<Boolean>() {
            @Override
            public void doSuccess(Boolean isAuthenticated) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingDelay = Math.max(0, SPLASH_DELAY - elapsedTime);

                handler.postDelayed(() -> {
                    checkPermissionsAndNavigate(Boolean.TRUE.equals(isAuthenticated));
                }, remainingDelay);
            }

            @Override
            public void doError(Throwable error) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingDelay = Math.max(0, SPLASH_DELAY - elapsedTime);

                handler.postDelayed(() -> {
                    checkPermissionsAndNavigate(false);
                }, remainingDelay);
            }

            @Override
            public void doSuccess() {}

            @Override
            public void doFail() {
                checkPermissionsAndNavigate(false);
            }
        });
    }

    private void checkPermissionsAndNavigate(boolean isAuthenticated) {
        if (isFinishing() || isDestroyed()) return;
        pendingNavigateToMain = isAuthenticated;

        String[] deniedPermissions = PermissionUtils.getDeniedPermissions(this);
        if (deniedPermissions.length > 0) {
            // Có quyền chưa cấp -> hỏi tất cả các quyền một thể
            multiplePermissionsLauncher.launch(deniedPermissions);
        } else {
            // Tất cả các quyền đã được cấp trước đó -> đi thẳng vào app
            proceedNavigation();
        }
    }

    private void proceedNavigation() {
        if (isFinishing() || isDestroyed()) return;
        if (pendingNavigateToMain) {
            navigateToMain();
        } else {
            navigateToLogin();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
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
