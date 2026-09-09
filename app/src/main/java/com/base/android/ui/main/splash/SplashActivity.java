package com.base.android.ui.main.splash;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.base.android.BR;
import com.base.android.R;
import com.base.android.databinding.ActivitySplashBinding;
import com.base.android.di.component.ActivityComponent;
import com.base.android.ui.base.activity.BaseActivity;
import com.base.android.ui.main.MainActivity;
import com.base.android.ui.main.MainCallback;
import com.base.android.ui.main.account.login.LoginActivity;

public class SplashActivity extends BaseActivity<ActivitySplashBinding, SplashViewModel> {

    private static final long SPLASH_DELAY = 1200L;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setupEdgeToEdge();
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        startSplashCheck();
    }

    private void setupEdgeToEdge() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        window.setStatusBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.login_background));
        }
    }

    private void startSplashCheck() {
        long startTime = System.currentTimeMillis();

        viewModel.checkAuthentication(new MainCallback<Boolean>() {
            @Override
            public void doSuccess(Boolean isAuthenticated) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingDelay = Math.max(0, SPLASH_DELAY - elapsedTime);

                handler.postDelayed(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    if (Boolean.TRUE.equals(isAuthenticated)) {
                        navigateToMain();
                    } else {
                        navigateToLogin();
                    }
                }, remainingDelay);
            }

            @Override
            public void doError(Throwable error) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingDelay = Math.max(0, SPLASH_DELAY - elapsedTime);

                handler.postDelayed(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    navigateToLogin();
                }, remainingDelay);
            }

            @Override
            public void doSuccess() {}

            @Override
            public void doFail() {
                navigateToLogin();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
