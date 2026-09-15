package com.base.android.ui.main.account.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.base.android.BR;
import com.base.android.R;
import com.base.android.data.model.api.response.user.UserLoginResponse;
import com.base.android.databinding.ActivityLoginBinding;
import com.base.android.di.component.ActivityComponent;
import com.base.android.ui.base.activity.BaseActivity;
import com.base.android.ui.main.MainActivity;
import com.base.android.ui.main.MainCallback;

public class LoginActivity extends BaseActivity<ActivityLoginBinding, LoginViewModel> {

    private boolean isPasswordVisible = false;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;
    private boolean isButtonShifted = false;
    private int lastKnownKeypadHeight = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setupEdgeToEdge();
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        initViews();
        setupKeyboardListener();
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

    private void initViews() {
        // IME action Done on password field
        viewBinding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                setButtonShifted(false);
                handleLogin();
                return true;
            }
            return false;
        });

        // Focus change listener: shift button up when input gains focus
        viewBinding.etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard(viewBinding.etUsername);
                setButtonShifted(true);
            }
        });

        viewBinding.etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard(viewBinding.etPassword);
                setButtonShifted(true);
            }
        });

        // Cho phép nút Đăng nhập hiển thị vượt ra ngoài container mà không bị clip
        viewBinding.rootLayout.setClipChildren(false);
        viewBinding.rootLayout.setClipToPadding(false);
        viewBinding.layoutBottomContainer.setClipChildren(false);
        viewBinding.layoutBottomContainer.setClipToPadding(false);
    }

    public void onInputClick(View view) {
        showKeyboard(view);
        setButtonShifted(true);
    }

    public void onPasswordContainerClick() {
        viewBinding.etPassword.requestFocus();
        showKeyboard(viewBinding.etPassword);
        setButtonShifted(true);
    }

    public void onRootClick() {
        hideKeyboard();
        setButtonShifted(false);
    }

    public void onQrCodeClick() {
        viewModel.showNormalMessage("Tính năng đăng nhập bằng mã QR đang được phát triển");
    }

    public void onForgotPasswordClick() {
        viewModel.showNormalMessage("Vui lòng liên hệ quản trị viên để đặt lại mật khẩu");
    }

    public void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            viewBinding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            viewBinding.ivTogglePassword.setImageResource(R.drawable.ic_visibility);
        } else {
            viewBinding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            viewBinding.ivTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        }
        viewBinding.etPassword.setSelection(viewBinding.etPassword.getText().length());
    }

    public void handleLogin() {
        hideKeyboard();
        setButtonShifted(false);

        viewModel.doLogin(new MainCallback<UserLoginResponse>() {
            @Override
            public void doSuccess(UserLoginResponse response) {
                viewModel.showSuccessMessage("Đăng nhập thành công!");
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finishAffinity();
            }

            @Override
            public void doError(Throwable error) {
                // Error message is handled in ViewModel
            }

            @Override
            public void doSuccess() {}

            @Override
            public void doFail() {}
        });
    }

    private void setButtonShifted(boolean shifted) {
        setButtonShifted(shifted, false);
    }

    private void setButtonShifted(boolean shifted, boolean forceUpdate) {
        if (isButtonShifted == shifted && !forceUpdate) return;
        isButtonShifted = shifted;

        if (shifted) {
            viewBinding.btnLogin.post(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    viewBinding.btnLogin.setTranslationZ(50f);
                }

                int[] btnLoc = new int[2];
                viewBinding.btnLogin.getLocationOnScreen(btnLoc);
                float currentBtnTop = btnLoc[1] - viewBinding.btnLogin.getTranslationY();

                int[] qrLoc = new int[2];
                viewBinding.layoutLoginQr.getLocationOnScreen(qrLoc);
                float qrBottom = qrLoc[1] + viewBinding.layoutLoginQr.getHeight();

                float targetMargin = getResources().getDimension(R.dimen._14sdp);
                float targetY = qrBottom + targetMargin;

                Rect r = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                int keyboardTop = r.bottom;
                int btnHeight = viewBinding.btnLogin.getHeight();

                // Đảm bảo nút không bị bàn phím che nếu bàn phím quá cao
                if (keyboardTop > 0 && targetY + btnHeight > keyboardTop - 10) {
                    targetY = (keyboardTop - 10) - btnHeight;
                }

                float shiftDistance = currentBtnTop - targetY;
                if (shiftDistance > 0) {
                    viewBinding.btnLogin.animate()
                            .translationY(-shiftDistance)
                            .setDuration(220)
                            .start();
                }
            });
        } else {
            viewBinding.btnLogin.animate()
                    .translationY(0f)
                    .setDuration(220)
                    .start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                viewBinding.btnLogin.setTranslationZ(0f);
            }
            viewBinding.scrollView.smoothScrollTo(0, 0);
        }
    }

    private void showKeyboard(View view) {
        if (view != null) {
            view.post(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
    }

    @Override
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View view = getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.clearFocus();
            } else {
                View rootView = getWindow().getDecorView().getRootView();
                if (rootView != null) {
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                }
            }
        }
    }

    private void setupKeyboardListener() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private int previousKeypadHeight = 0;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                // Keyboard is open (height > 15% of screen height)
                boolean keyboardOpen = keypadHeight > screenHeight * 0.15;
                if (keyboardOpen) {
                    boolean heightChanged = (keypadHeight != lastKnownKeypadHeight);
                    lastKnownKeypadHeight = keypadHeight;
                    setButtonShifted(true, heightChanged);
                } else if (previousKeypadHeight > screenHeight * 0.15) {
                    lastKnownKeypadHeight = 0;
                    setButtonShifted(false, false);
                }
                previousKeypadHeight = keypadHeight;
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRectUsername = new Rect();
                Rect outRectPassword = new Rect();
                viewBinding.etUsername.getGlobalVisibleRect(outRectUsername);
                viewBinding.flPasswordInput.getGlobalVisibleRect(outRectPassword);
                int x = (int) ev.getRawX();
                int y = (int) ev.getRawY();

                // If tap is outside both EditText areas, hide keyboard and return button
                if (!outRectUsername.contains(x, y) && !outRectPassword.contains(x, y)) {
                    hideKeyboard();
                    setButtonShifted(false);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        if (keyboardLayoutListener != null) {
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            if (rootView != null) {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
            }
        }
        super.onDestroy();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
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
