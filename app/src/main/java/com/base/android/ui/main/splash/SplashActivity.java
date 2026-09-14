package com.base.android.ui.main.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

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
