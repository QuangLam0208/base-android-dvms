package com.base.android.ui.main.courses.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.base.android.R;
import com.base.android.data.model.api.response.course.CourseResponse;
import com.base.android.data.model.api.response.course.SyllabusResponse;
import com.base.android.databinding.ItemCourseBinding;
import com.base.android.helper.ThemeHelper;
import com.base.android.ui.base.adapter.OnItemClickListener;
import com.base.android.utils.HangingBulletSpan;
import com.base.android.utils.ImageUtils;
import com.bumptech.glide.Glide;

import android.text.SpannableStringBuilder;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    public interface OnSeeMoreListener {
        void onSeeMore(CourseResponse course, int position);
    }

    private final List<CourseResponse> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private OnSeeMoreListener seeMoreListener;
    private final Context context;
    private boolean isNightMode;

    public CourseAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.isNightMode = ThemeHelper.isDarkMode(context);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNightMode(boolean isNightMode) {
        this.isNightMode = isNightMode;
        notifyDataSetChanged();
    }

    public void setOnSeeMoreListener(OnSeeMoreListener seeMoreListener) {
        this.seeMoreListener = seeMoreListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<CourseResponse> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public void setSyllabusData(int position, List<SyllabusResponse> syllabuses) {
        if (position >= 0 && position < items.size()) {
            CourseResponse course = items.get(position);
            course.setLoadingSyllabus(false);
            course.setSyllabuses(syllabuses);
            course.setSyllabusExpanded(true);
            notifyItemChanged(position);
        }
    }

    public void setSyllabusError(int position) {
        if (position >= 0 && position < items.size()) {
            CourseResponse course = items.get(position);
            course.setLoadingSyllabus(false);
            course.setSyllabusExpanded(false);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCourseBinding binding = ItemCourseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CourseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        holder.binding.setIsNightMode(isNightMode);
        holder.bind(items.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class CourseViewHolder extends RecyclerView.ViewHolder {

        private final ItemCourseBinding binding;
        private SyllabusAdapter syllabusAdapter;

        public CourseViewHolder(@NonNull ItemCourseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.tvViewMore.setPaintFlags(this.binding.tvViewMore.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        }

        public void bind(CourseResponse course) {
            if (course == null) return;

            // Tên khoá học
            String courseName = !TextUtils.isEmpty(course.getName()) ? course.getName() : "";
            binding.tvCourseName.setText(courseName);

            // Giá
            Double price = course.getPrice();
            if (price != null && price > 0) {
                DecimalFormat formatter = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));
                binding.tvCoursePrice.setText(formatter.format(price) + " đ");
            } else {
                binding.tvCoursePrice.setText("Miễn phí");
            }

            // Mô tả dạng bullet list có hiệu ứng hanging indent (Spannable + LeadingMarginSpan)
            String description = course.getShortDescription();
            if (!TextUtils.isEmpty(description)) {
                int bulletRadius = context.getResources().getDimensionPixelSize(R.dimen.course_bullet_radius);
                int bulletGap = context.getResources().getDimensionPixelSize(R.dimen.course_bullet_gap);
                int bulletMarginStart = context.getResources().getDimensionPixelSize(R.dimen.course_bullet_margin_start);
                int bulletColor = ContextCompat.getColor(context, R.color.course_short_description);

                SpannableStringBuilder bulletSpannable = HangingBulletSpan.formatBulletList(
                        description,
                        bulletRadius,
                        bulletGap,
                        bulletMarginStart,
                        bulletColor
                );
                binding.tvCourseDescription.setText(bulletSpannable);
                binding.tvCourseDescription.setVisibility(View.VISIBLE);
            } else {
                binding.tvCourseDescription.setText("");
                binding.tvCourseDescription.setVisibility(View.GONE);
            }

            // Avatar
            String avatar = course.getAvatar();
            if (avatar != null && ("null".equalsIgnoreCase(avatar.trim()) || avatar.trim().isEmpty())) {
                avatar = null;
            }
            Glide.with(context)
                    .load(ImageUtils.getFullImageUrl(avatar))
                    .placeholder(R.drawable.ic_course_placeholder)
                    .error(R.drawable.ic_course_placeholder)
                    .into(binding.ivCourseAvatar);

            // Click vào thẻ khoá học -> xem chi tiết khoá học
            binding.cardCourse.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(pos);
                }
            });

            // Click vào nút "Xem thêm" -> toggle hiển thị Syllabus
            binding.btnViewMore.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                if (course.isSyllabusExpanded()) {
                    course.setSyllabusExpanded(false);
                    notifyItemChanged(pos);
                } else {
                    if (course.getSyllabuses() != null) {
                        course.setSyllabusExpanded(true);
                        notifyItemChanged(pos);
                    } else {
                        course.setSyllabusExpanded(true);
                        course.setLoadingSyllabus(true);
                        notifyItemChanged(pos);
                        if (seeMoreListener != null) {
                            seeMoreListener.onSeeMore(course, pos);
                        }
                    }
                }
            });

            // Quản lý hiển thị phần Giáo trình (Syllabus)
            boolean isSyllabusExpanded = course.isSyllabusExpanded();
            if (isSyllabusExpanded) {
                binding.cardSyllabus.setVisibility(View.VISIBLE);
                binding.ivArrowMore.setRotation(180f);

                if (course.isLoadingSyllabus()) {
                    binding.pbSyllabusLoading.setVisibility(View.VISIBLE);
                    binding.rvSyllabuses.setVisibility(View.GONE);
                    binding.tvSyllabusEmpty.setVisibility(View.GONE);
                } else {
                    binding.pbSyllabusLoading.setVisibility(View.GONE);
                    List<SyllabusResponse> syllabuses = course.getSyllabuses();
                    if (syllabuses != null && !syllabuses.isEmpty()) {
                        binding.tvSyllabusEmpty.setVisibility(View.GONE);
                        binding.rvSyllabuses.setVisibility(View.VISIBLE);

                        if (syllabusAdapter == null) {
                            syllabusAdapter = new SyllabusAdapter(context);
                            binding.rvSyllabuses.setLayoutManager(new LinearLayoutManager(context));
                            binding.rvSyllabuses.setAdapter(syllabusAdapter);
                        }
                        syllabusAdapter.setNightMode(isNightMode);
                        syllabusAdapter.setData(syllabuses);
                    } else {
                        binding.tvSyllabusEmpty.setVisibility(View.VISIBLE);
                        binding.rvSyllabuses.setVisibility(View.GONE);
                    }
                }
            } else {
                binding.cardSyllabus.setVisibility(View.GONE);
                binding.ivArrowMore.setRotation(0f);
            }
        }
    }
}
