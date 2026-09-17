package com.base.android.ui.main.reviews.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.base.android.R;
import com.base.android.data.model.api.response.course.CourseResponse;
import com.base.android.data.model.api.response.rating.RatingResponse;
import com.base.android.databinding.ItemReviewBinding;
import com.base.android.helper.ThemeHelper;
import com.base.android.ui.base.adapter.OnItemClickListener;
import com.base.android.utils.ImageUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final List<RatingResponse> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private final Context context;
    private boolean isNightMode;

    public ReviewAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.isNightMode = ThemeHelper.isDarkMode(context);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNightMode(boolean isNightMode) {
        this.isNightMode = isNightMode;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<RatingResponse> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReviewBinding binding = ItemReviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.binding.setIsNightMode(isNightMode);
        holder.bind(items.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemReviewBinding binding;
        public ReviewViewHolder(@NonNull ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        public void bind(RatingResponse rating) {
            // 1. Tên học viên & Avatar
            RatingResponse.RatingStudent student = rating.getStudent();
            RatingResponse.RatingAccount account = student != null ? student.getAccount() : null;
            String studentName = (account != null && !TextUtils.isEmpty(account.getFullName()))
                    ? account.getFullName()
                    : "Học viên";
            binding.tvStudentName.setText(studentName);
            String avatarPath = (account != null) ? account.getAvatarPath() : null;
            Glide.with(context)
                    .load(ImageUtils.getFullImageUrl(avatarPath))
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.ivStudentAvatar);
            // 2. Tên khóa học
            CourseResponse course = rating.getCourse();
            String courseName = (course != null && !TextUtils.isEmpty(course.getName()))
                    ? course.getName()
                    : "";
            binding.tvCourseName.setText(courseName);
            // 3. Số sao đánh giá
            int star = rating.getStar() != null ? rating.getStar() : 5;
            binding.ratingBar.setRating(star);
            // 4. Lời nhận xét
            String message = !TextUtils.isEmpty(rating.getMessage()) ? rating.getMessage() : "";
            binding.tvReviewMessage.setText(message);
            // Click listener
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
