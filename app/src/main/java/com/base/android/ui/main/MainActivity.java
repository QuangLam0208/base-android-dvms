package com.base.android.ui.main;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.base.android.BR;
import com.base.android.R;
import com.base.android.databinding.ActivityMainBinding;
import com.base.android.di.component.ActivityComponent;
import com.base.android.ui.base.activity.BaseActivity;


public class MainActivity extends BaseActivity<ActivityMainBinding, MainViewModel> {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

    }
    @Override
    protected void onResume() {
        super.onResume();

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
