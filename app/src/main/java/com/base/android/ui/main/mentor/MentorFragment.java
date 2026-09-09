package com.base.android.ui.main.mentor;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.base.android.BR;
import com.base.android.R;
import com.base.android.data.model.api.response.mentor.MentorResponse;
import com.base.android.databinding.FragmentMentorBinding;
import com.base.android.di.component.FragmentComponent;
import com.base.android.ui.base.adapter.OnItemClickListener;
import com.base.android.ui.base.fragment.BaseFragment;
import com.base.android.ui.main.MainCallback;
import com.base.android.ui.main.mentor.adapter.MentorAdapter;

import java.util.List;

public class MentorFragment extends BaseFragment<FragmentMentorBinding, MentorViewModel> {

    private MentorAdapter mentorAdapter;

    public static MentorFragment newInstance() {
        return new MentorFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        initSwipeRefresh();
        loadData();
    }

    private void initRecyclerView() {
        mentorAdapter = new MentorAdapter(getContext(), new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // TODO: Xử lý click vào item mentor nếu cần
            }

            @Override
            public void onItemDelete(int position) {
            }
        });
        binding.rvMentors.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMentors.setAdapter(mentorAdapter);
    }

    private void initSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        binding.layoutEmpty.setVisibility(View.GONE);

        viewModel.getPublicMentors(new MainCallback<List<MentorResponse>>() {
            @Override
            public void doSuccess(List<MentorResponse> list) {
                binding.swipeRefreshLayout.setRefreshing(false);
                if (list != null && !list.isEmpty()) {
                    mentorAdapter.setData(list);
                    binding.layoutEmpty.setVisibility(View.GONE);
                } else {
                    mentorAdapter.setData(null);
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
                viewModel.showErrorMessage("Không thể tải danh sách mentor.");
            }
        });
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mentor;
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
        if (!hidden && mentorAdapter != null && mentorAdapter.getItemCount() == 0) {
            loadData();
        }
    }
}
