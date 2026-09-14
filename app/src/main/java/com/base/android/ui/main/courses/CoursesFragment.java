package com.base.android.ui.main.courses;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.base.android.BR;
import com.base.android.R;
import com.base.android.data.model.api.response.course.CourseResponse;
import com.base.android.data.model.api.response.course.SyllabusResponse;
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
        courseAdapter.setOnSeeMoreListener((course, position) -> {
            if (course != null && course.getId() != null) {
                loadSyllabus(course.getId(), position);
            }
        });
        binding.rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCourses.setAdapter(courseAdapter);
    }

    private void loadSyllabus(Long courseId, int position) {
        viewModel.getListSyllabus(courseId, new MainCallback<List<SyllabusResponse>>() {
            @Override
            public void doSuccess(List<SyllabusResponse> syllabuses) {
                courseAdapter.setSyllabusData(position, syllabuses);
            }

            @Override
            public void doSuccess() {}

            @Override
            public void doError(Throwable error) {
                courseAdapter.setSyllabusError(position);
                viewModel.showErrorMessage(getString(R.string.error_load_syllabus));
            }

            @Override
            public void doFail() {
                courseAdapter.setSyllabusError(position);
                viewModel.showErrorMessage(getString(R.string.error_load_syllabus));
            }
        });
    }

    private void initSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadData);
    }

    private void showShimmer() {
        if (binding != null && binding.shimmerViewContainer != null) {
            binding.shimmerViewContainer.setVisibility(View.VISIBLE);
            binding.shimmerViewContainer.startShimmer();
        }
        if (binding != null) {
            binding.rvCourses.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void hideShimmer() {
        if (binding != null && binding.shimmerViewContainer != null) {
            binding.shimmerViewContainer.stopShimmer();
            binding.shimmerViewContainer.setVisibility(View.GONE);
        }
    }

    private void loadData() {
        boolean isPullToRefresh = binding.swipeRefreshLayout.isRefreshing();
        if (!isPullToRefresh) {
            showShimmer();
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
        }

        viewModel.getListCourse(new MainCallback<List<CourseResponse>>() {
            @Override
            public void doSuccess(List<CourseResponse> list) {
                hideShimmer();
                binding.swipeRefreshLayout.setRefreshing(false);
                if (list != null && !list.isEmpty()) {
                    courseAdapter.setData(list);
                    binding.rvCourses.setVisibility(View.VISIBLE);
                    binding.layoutEmpty.setVisibility(View.GONE);
                } else {
                    courseAdapter.setData(null);
                    binding.rvCourses.setVisibility(View.GONE);
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void doSuccess() {}

            @Override
            public void doError(Throwable error) {
                hideShimmer();
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.rvCourses.setVisibility(View.VISIBLE);
                viewModel.showErrorMessage(getString(R.string.newtwork_error));
            }

            @Override
            public void doFail() {
                hideShimmer();
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.rvCourses.setVisibility(View.VISIBLE);
                viewModel.showErrorMessage(getString(R.string.error_load_courses));
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

    @Override
    public void onDestroyView() {
        if (binding != null && binding.shimmerViewContainer != null) {
            binding.shimmerViewContainer.stopShimmer();
        }
        super.onDestroyView();
    }
}
