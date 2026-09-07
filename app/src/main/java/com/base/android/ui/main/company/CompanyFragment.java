package com.base.android.ui.main.company;

import com.base.android.BR;

import com.base.android.R;
import com.base.android.databinding.FragmentCompanyBinding;
import com.base.android.di.component.FragmentComponent;
import com.base.android.ui.base.fragment.BaseFragment;

public class CompanyFragment extends BaseFragment<FragmentCompanyBinding, CompanyViewModel> {

    public static CompanyFragment newInstance() {
        return new CompanyFragment();
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_company;
    }

    @Override
    protected void performDataBinding() {
        binding.setF(this);
        binding.setVm(viewModel);
    }

    @Override
    protected void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            // refresh dữ liệu hoặc track Analytics tại đây
        }
    }
}
