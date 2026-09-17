package com.base.android.ui.base.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.base.android.MVVMApplication;
import com.base.android.R;
import com.base.android.di.component.DaggerFragmentComponent;
import com.base.android.di.component.FragmentComponent;
import com.base.android.di.module.FragmentModule;
import com.base.android.helper.ThemeHelper;
import com.base.android.ui.base.activity.BaseActivity;

import javax.inject.Inject;
import javax.inject.Named;

public abstract class BaseFragment <B extends ViewDataBinding,V extends BaseFragmentViewModel> extends Fragment {

    protected B binding;
    @Inject
    protected V viewModel;

    @Named("access_token")
    @Inject
    protected String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        performDependencyInjection(getBuildComponent());
        binding = DataBindingUtil.inflate(inflater,getLayoutId(),container,false);
        binding.setVariable(getBindingVariable(),viewModel);
        performDataBinding();
        viewModel.setToken(token);
        viewModel.mErrorMessage.observe(getViewLifecycleOwner(),toastMessage -> {
            if (toastMessage!=null){
                toastMessage.showMessage(requireContext());
            }
        });
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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getContext() != null && viewModel != null) {
            boolean isDark = ThemeHelper.isDarkMode(getContext());
            viewModel.setNightMode(isDark);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndSyncNightMode(); // check khi fragment hiện
    }

    /**
     * Kích hoạt khi chuyển tab khác.
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            checkAndSyncNightMode();
        }
    }

    /**
     * Check và sync theme với chế độ hiện tại
     */
    public void checkAndSyncNightMode() {
        if (getContext() == null || viewModel == null) return;
        boolean isDark = ThemeHelper.isDarkMode(getContext());
        if (viewModel.isNightMode.get() != isDark) {
            viewModel.setNightMode(isDark);
            onThemeChanged(isDark);
        }
    }

    /**
     * Hook cho Fragment con khi theme thay đổi (ví dụ: cập nhật adapter hoặc re-render custom view).
     */
    protected void onThemeChanged(boolean isDark) {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public abstract int getBindingVariable();

    protected abstract int getLayoutId();

    protected abstract void performDataBinding();

    protected abstract void performDependencyInjection(FragmentComponent buildComponent);

    private FragmentComponent getBuildComponent(){
        return DaggerFragmentComponent.builder()
                .appComponent(((MVVMApplication) requireActivity().getApplication()).getAppComponent())
                .fragmentModule(new FragmentModule(this))
                .build();
    }

    public void showProgressbar(String msg){
        if (requireActivity() instanceof BaseActivity) {
            ((BaseActivity<?, ?>) requireActivity()).showProgressbar(msg);
        }
    }

    public void hideProgress() {
        if (requireActivity() instanceof BaseActivity) {
            ((BaseActivity<?, ?>) requireActivity()).hideProgress();
        }
    }

}
