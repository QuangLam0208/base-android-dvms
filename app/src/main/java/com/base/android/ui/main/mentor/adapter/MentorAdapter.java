package com.base.android.ui.main.mentor.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.base.android.R;
import com.base.android.data.model.api.response.mentor.MentorAccountResponse;
import com.base.android.data.model.api.response.mentor.MentorResponse;
import com.base.android.databinding.ItemMentorBinding;
import com.base.android.helper.ThemeHelper;
import com.base.android.ui.base.adapter.OnItemClickListener;
import com.base.android.utils.ImageUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MentorAdapter extends RecyclerView.Adapter<MentorAdapter.MentorViewHolder> {

    private final List<MentorResponse> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private final Context context;
    private boolean isNightMode;

    public MentorAdapter(Context context, OnItemClickListener listener) {
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
    public void setData(List<MentorResponse> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MentorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMentorBinding binding = ItemMentorBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MentorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MentorViewHolder holder, int position) {
        holder.binding.setIsNightMode(isNightMode);
        holder.bind(items.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MentorViewHolder extends RecyclerView.ViewHolder {

        private final ItemMentorBinding binding;

        public MentorViewHolder(@NonNull ItemMentorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MentorResponse mentor) {
            MentorAccountResponse account = mentor.getAccount();

            // Tên Mentor
            String fullName = (account != null && !TextUtils.isEmpty(account.getFullName()))
                    ? account.getFullName()
                    : "";
            binding.tvMentorName.setText(fullName);

            // Vị trí chuyên môn
            String position = !TextUtils.isEmpty(mentor.getPosition())
                    ? mentor.getPosition()
                    : "";
            binding.tvMentorPosition.setText(position);

            // Mô tả
            String description = !TextUtils.isEmpty(mentor.getDescription())
                    ? mentor.getDescription()
                    : "";
            binding.tvMentorDescription.setText(description);

            // Avatar
            String avatarPath = (account != null) ? account.getAvatarPath() : null;
            String fullImageUrl = ImageUtils.getFullImageUrl(avatarPath);

            Glide.with(context)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(binding.ivMentorAvatar);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
