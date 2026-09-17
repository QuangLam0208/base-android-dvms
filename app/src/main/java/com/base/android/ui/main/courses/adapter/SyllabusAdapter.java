package com.base.android.ui.main.courses.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.base.android.R;
import com.base.android.data.model.api.response.course.SyllabusResponse;
import com.base.android.databinding.ItemSyllabusBinding;
import com.base.android.helper.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

public class SyllabusAdapter extends RecyclerView.Adapter<SyllabusAdapter.SyllabusViewHolder> {

    private final List<SyllabusResponse> items = new ArrayList<>();
    private final Context context;
    private boolean isNightMode;

    public SyllabusAdapter(Context context) {
        this.context = context;
        this.isNightMode = ThemeHelper.isDarkMode(context);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNightMode(boolean isNightMode) {
        this.isNightMode = isNightMode;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<SyllabusResponse> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SyllabusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSyllabusBinding binding = ItemSyllabusBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SyllabusViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SyllabusViewHolder holder, int position) {
        holder.binding.setIsNightMode(isNightMode);
        holder.bind(items.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class SyllabusViewHolder extends RecyclerView.ViewHolder {

        private final ItemSyllabusBinding binding;

        public SyllabusViewHolder(@NonNull ItemSyllabusBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SyllabusResponse syllabus) {
            if (syllabus == null) return;

            String name = !TextUtils.isEmpty(syllabus.getName()) ? syllabus.getName() : "";
            binding.tvSyllabusName.setText(name);

            String description = !TextUtils.isEmpty(syllabus.getDescription()) ? syllabus.getDescription() : "";
            binding.tvSyllabusDescription.setText(description);

            boolean isExpanded = syllabus.isExpanded();
            if (isExpanded && !TextUtils.isEmpty(description)) {
                binding.tvSyllabusDescription.setVisibility(View.VISIBLE);
                binding.ivExpandIcon.setImageResource(R.drawable.ic_minus);
            } else {
                binding.tvSyllabusDescription.setVisibility(View.GONE);
                binding.ivExpandIcon.setImageResource(R.drawable.ic_plus);
            }

            View.OnClickListener toggleClick = v -> {
                int currentPos = getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    syllabus.setExpanded(!syllabus.isExpanded());
                    notifyItemChanged(currentPos);
                }
            };

            binding.getRoot().setOnClickListener(toggleClick);
            binding.ivExpandIcon.setOnClickListener(toggleClick);
        }
    }
}
