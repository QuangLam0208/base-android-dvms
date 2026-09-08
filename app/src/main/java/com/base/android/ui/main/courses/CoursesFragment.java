package com.base.android.ui.main.courses;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.base.android.BR;
import com.base.android.R;
import com.base.android.data.model.api.response.classroom.ClassRoomResponse;
import com.base.android.databinding.FragmentCoursesBinding;
import com.base.android.di.component.FragmentComponent;
import com.base.android.ui.base.adapter.OnItemClickListener;
import com.base.android.ui.base.fragment.BaseFragment;
import com.base.android.ui.main.MainCallback;
import com.base.android.ui.main.courses.adapter.CourseAdapter;

import java.util.List;

public class CoursesFragment extends BaseFragment<FragmentCoursesBinding, CoursesViewModel> {

    private CourseAdapter courseAdapter;

    public static CoursesFragment newInstance() {
        return new CoursesFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        initSwipeRefresh();
        loadData();
    }

    private void initRecyclerView() {
        courseAdapter = new CourseAdapter(getContext(), new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // TODO: navigate to course detail
            }

            @Override
            public void onItemDelete(int position) {
                // not used
            }
        });
        binding.rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCourses.setAdapter(courseAdapter);
    }

    private void initSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadData);
    }

    private void loadData() {
        binding.layoutEmpty.setVisibility(View.GONE);

        viewModel.getListCourse(new MainCallback<List<ClassRoomResponse>>() {
            @Override
            public void doSuccess(List<ClassRoomResponse> list) {
                binding.swipeRefreshLayout.setRefreshing(false);
                if (list != null && !list.isEmpty()) {
                    courseAdapter.setData(list);
                    binding.layoutEmpty.setVisibility(View.GONE);
                } else {
                    courseAdapter.setData(null);
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
                viewModel.showErrorMessage("Không thể tải danh sách khoá học.");
            }
        });
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_courses;
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
            // Có thể refresh dữ liệu khi tab được hiển thị lại
        }
    }
}
