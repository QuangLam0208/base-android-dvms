package com.base.android.ui.main.courses.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.base.android.R;
import com.base.android.data.model.api.response.classroom.ClassRoomResponse;
import com.base.android.data.model.api.response.course.CourseResponse;
import com.base.android.databinding.ItemCourseBinding;
import com.base.android.ui.base.adapter.OnItemClickListener;
import com.base.android.utils.ImageUtils;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private final List<ClassRoomResponse> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private final Context context;

    public CourseAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<ClassRoomResponse> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
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
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class CourseViewHolder extends RecyclerView.ViewHolder {

        private final ItemCourseBinding binding;

        public CourseViewHolder(@NonNull ItemCourseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ClassRoomResponse classRoom) {
            CourseResponse course = classRoom.getCourse();
            
            // Tên khoá học
            String courseName = (course != null && !TextUtils.isEmpty(course.getName()))
                    ? course.getName()
                    : "";
            binding.tvCourseName.setText(courseName);

            // Giá
            Double price = classRoom.getPrice();
            if (price == null && course != null) {
                price = course.getPrice();
            }
            if (price != null && price > 0) {
                NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                binding.tvCoursePrice.setText(formatter.format(price) + " đ");
            } else {
                binding.tvCoursePrice.setText("Miễn phí");
            }

            // Mô tả
            String description = (course != null && !TextUtils.isEmpty(course.getShortDescription())) ? course.getShortDescription() : "";
            binding.tvCourseDescription.setText(description);

            // Avatar
            String avatar = (course != null) ? course.getAvatar() : null;
            Glide.with(context)
                    .load(ImageUtils.getFullImageUrl(avatar))
                    .placeholder(R.drawable.ic_course_placeholder)
                    .error(R.drawable.ic_course_placeholder)
                    .into(binding.ivCourseAvatar);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(getAdapterPosition());
            });

            binding.btnViewMore.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(getAdapterPosition());
            });
        }
    }
}


