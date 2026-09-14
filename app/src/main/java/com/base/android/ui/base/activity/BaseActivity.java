package com.base.android.ui.base.activity;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.base.android.MVVMApplication;
import com.base.android.R;
import com.base.android.constant.Constants;
import com.base.android.data.local.prefs.AppPreferencesService;
import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.di.component.ActivityComponent;
import com.base.android.di.component.DaggerActivityComponent;
import com.base.android.di.module.ActivityModule;
import com.base.android.helper.LocaleHelper;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.inject.Named;

public abstract class BaseActivity<B extends ViewDataBinding, V extends BaseViewModel> extends AppCompatActivity{

    @Override
    protected void attachBaseContext(Context newBase) {
        PreferencesService prefs = new AppPreferencesService(newBase, Constants.PREF_NAME, new Gson());
        String langCode = prefs.getAppLanguage();
        Context context = LocaleHelper.setLocale(newBase, langCode);
        super.attachBaseContext(context);
    }

    protected B viewBinding;

    @Inject
    protected V viewModel;

    @Inject
    protected Context application;

    @Named("access_token")
    @Inject
    protected String token;

    @Named("device_id")
    @Inject
    protected String deviceId;

    private View loadingOverlay;
    // Listen all action from local
    private BroadcastReceiver globalApplicationReceiver;
    private IntentFilter filterGlobalApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        performDependencyInjection(getBuildComponent());
        super.onCreate(savedInstanceState);
        performDataBinding();
        updateCurrentAcitivity();

        viewModel.setToken(token);
        viewModel.setDeviceId(deviceId);
        viewModel.mIsLoading.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback(){

            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if(((ObservableBoolean)sender).get()){
                    showProgressbar(getResources().getString(R.string.msg_loading));
                }else{
                    hideProgress();
                }
            }
        });
        viewModel.mErrorMessage.observe(this, toastMessage -> {
            if(toastMessage!=null){
                toastMessage.showMessage(getApplicationContext());
            }
        });
        viewModel.progressBarMsg.observe(this, progressBarMsg ->{
            if (progressBarMsg != null){
                changeProgressBarMsg(progressBarMsg);
            }
        });
        filterGlobalApplication = new IntentFilter();
        filterGlobalApplication.addAction(Constants.ACTION_EXPIRED_TOKEN);
        globalApplicationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action==null){
                    return;
                }
                if (action.equals(Constants.ACTION_EXPIRED_TOKEN)){
                    doExpireSession();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(globalApplicationReceiver, filterGlobalApplication);
        updateCurrentAcitivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(globalApplicationReceiver);
    }

    public abstract @LayoutRes int getLayoutId();

    public abstract int getBindingVariable();

    public void doExpireSession() {
        //implement later

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void performDataBinding() {
        viewBinding = DataBindingUtil.setContentView(this, getLayoutId());
        viewBinding.setVariable(getBindingVariable(), viewModel);
        viewBinding.executePendingBindings();
        loadingOverlay = LayoutInflater.from(this).inflate(R.layout.layout_loading_overlay, null);
        ((ViewGroup) findViewById(android.R.id.content)).addView(loadingOverlay,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * Shows the shared full-screen loading overlay. The msg parameter is unused, kept for
     * source compatibility with existing callers.
     */
    public void showProgressbar(String msg){
        loadingOverlay.setVisibility(View.VISIBLE);
        android.widget.ImageView spinner = loadingOverlay.findViewById(R.id.iv_loading_spinner);
        if (spinner != null) {
            android.view.animation.RotateAnimation rotate = new android.view.animation.RotateAnimation(
                    0f, 360f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(900);
            rotate.setRepeatCount(android.view.animation.Animation.INFINITE);
            rotate.setInterpolator(new android.view.animation.LinearInterpolator());
            spinner.startAnimation(rotate);
        }
    }

    /**
     * No-op: the shared loading overlay is a spinner-only square with no message row.
     * Kept for source compatibility with existing callers.
     */
    public void changeProgressBarMsg(String msg){
        // intentionally empty — loading overlay shows no text
    }

    public void hideProgress() {
        loadingOverlay.setVisibility(View.GONE);
        android.widget.ImageView spinner = loadingOverlay.findViewById(R.id.iv_loading_spinner);
        if (spinner != null) {
            spinner.clearAnimation();
        }
    }


    private ActivityComponent getBuildComponent() {
        return DaggerActivityComponent.builder()
                .appComponent(((MVVMApplication)getApplication()).getAppComponent())
                .activityModule(new ActivityModule(this))
                .build();
    }

    public abstract void performDependencyInjection(ActivityComponent buildComponent);

    private void updateCurrentAcitivity(){
        MVVMApplication mvvmApplication = (MVVMApplication)application;
        mvvmApplication.setCurrentActivity(this);
    }

    public boolean showHeader(){
        return false;
    }

    ObservableField<String> leftTitle;
    ObservableField<String> centerTitle;
    public void setCenterTitle(String msg){
        if (centerTitle == null){
            centerTitle = new ObservableField<>(msg);
        } else {
            centerTitle.set(msg);
        }
    }
    public void setLeftTitle(String msg){
        if (leftTitle == null){
            leftTitle = new ObservableField<>(msg);
        } else {
            leftTitle.set(msg);
        }
    }
}
