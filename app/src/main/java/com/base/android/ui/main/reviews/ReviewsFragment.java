package com.base.android.ui.main.reviews;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.base.android.BR;

import com.base.android.R;
import com.base.android.data.model.api.response.rating.RatingResponse;
import com.base.android.databinding.FragmentReviewsBinding;
import com.base.android.di.component.FragmentComponent;
import com.base.android.ui.base.adapter.OnItemClickListener;
import com.base.android.ui.base.fragment.BaseFragment;
import com.base.android.ui.main.MainCallback;
import com.base.android.ui.main.reviews.adapter.ReviewAdapter;

import java.util.List;

public class ReviewsFragment extends BaseFragment<FragmentReviewsBinding, ReviewsViewModel> {

    private ReviewAdapter reviewAdapter;

    public static ReviewsFragment newInstance() {
        return new ReviewsFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        initSwipeRefresh();
        loadData();
    }

    private void initRecyclerView() {
        reviewAdapter = new ReviewAdapter(getContext(), new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Xử lý sự kiện click item đánh giá
            }

            @Override
            public void onItemDelete(int position) {}
        });
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReviews.setAdapter(reviewAdapter);
    }

    private void initSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        binding.layoutEmpty.setVisibility(View.GONE);

        viewModel.getPublicRatings(new MainCallback<List<RatingResponse>>() {

            @Override
            public void doSuccess(List<RatingResponse> list) {
                binding.swipeRefreshLayout.setRefreshing(false);
                if (list != null && !list.isEmpty()) {
                    reviewAdapter.setData(list);
                    binding.layoutEmpty.setVisibility(View.GONE);
                } else {
                    reviewAdapter.setData(null);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void doSuccess() {}

            @Override
            public void doError(Throwable error) {
                binding.swipeRefreshLayout.setRefreshing(false);
                viewModel.showErrorMessage("Lỗi kết nối, vui lòng thử lại!");
            }

            @Override
            public void doFail() {
                binding.swipeRefreshLayout.setRefreshing(false);
                viewModel.showErrorMessage("Không thể tải danh sách đánh giá.");
            }
        });
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_reviews;
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
        if (!hidden && reviewAdapter != null && reviewAdapter.getItemCount() == 0) {
            loadData();
        }
    }
}
