package com.base.android.ui.main.company;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.base.android.BR;

import com.base.android.R;
import com.base.android.data.model.api.response.company.CompanyResponse;
import com.base.android.databinding.FragmentCompanyBinding;
import com.base.android.di.component.FragmentComponent;
import com.base.android.ui.base.adapter.OnItemClickListener;
import com.base.android.ui.base.fragment.BaseFragment;
import com.base.android.ui.main.MainCallback;
import com.base.android.ui.main.company.adapter.CompanyAdapter;

import java.util.List;

public class CompanyFragment extends BaseFragment<FragmentCompanyBinding, CompanyViewModel> {

    private CompanyAdapter companyAdapter;

    public static CompanyFragment newInstance() {
        return new CompanyFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        initSwipeRefresh();
        loadData();
    }

    private void initRecyclerView() {
        companyAdapter = new CompanyAdapter(getContext(), new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Khi click vào công ty
            }

            @Override
            public void onItemDelete(int position) {}
        });
        binding.rvCompanies.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvCompanies.setAdapter(companyAdapter);
    }

    private void initSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        binding.layoutEmpty.setVisibility(View.GONE);
        viewModel.getPublicCompanies(new MainCallback<List<CompanyResponse>>() {
            @Override
            public void doSuccess(List<CompanyResponse> list) {
                binding.swipeRefreshLayout.setRefreshing(false);
                if (list != null && !list.isEmpty()) {
                    companyAdapter.setData(list);
                    binding.layoutEmpty.setVisibility(View.GONE);
                } else {
                    companyAdapter.setData(null);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void doSuccess() {}

            @Override
            public void doError(Throwable error) {
                binding.swipeRefreshLayout.setRefreshing(false);
                viewModel.showErrorMessage(getString(R.string.newtwork_error));
            }

            @Override
            public void doFail() {
                binding.swipeRefreshLayout.setRefreshing(false);
                viewModel.showErrorMessage(getString(R.string.error_load_companies));
            }
        });
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
        if (!hidden && companyAdapter != null && companyAdapter.getItemCount() != 0) {
            loadData();
        }
    }
}
