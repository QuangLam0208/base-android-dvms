package com.base.android.ui.main.company.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.base.android.R;
import com.base.android.data.model.api.response.company.CompanyResponse;
import com.base.android.databinding.ItemCompanyBinding;
import com.base.android.helper.ThemeHelper;
import com.base.android.ui.base.adapter.OnItemClickListener;
import com.base.android.utils.ImageUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder> {

    private final List<CompanyResponse> items = new ArrayList<>();
    private final OnItemClickListener listener;
    private final Context context;
    private boolean isNightMode;

    public CompanyAdapter(Context context, OnItemClickListener listener) {
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
    public void setData(List<CompanyResponse> newData) {
        items.clear();
        if (newData != null) {
            items.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCompanyBinding binding = ItemCompanyBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CompanyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanyViewHolder holder, int position) {
        holder.binding.setIsNightMode(isNightMode);
        holder.bind(items.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class CompanyViewHolder extends RecyclerView.ViewHolder {
        private final ItemCompanyBinding binding;
        public CompanyViewHolder(@NonNull ItemCompanyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CompanyResponse company) {
            String name = !TextUtils.isEmpty(company.getName()) ? company.getName() : "";
            binding.tvCompanyName.setText(name);
            // Logo công ty
            String avatarUrl = ImageUtils.getFullImageUrl(company.getAvatar());
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_company)
                    .error(R.drawable.ic_company)
                    .circleCrop()
                    .into(binding.ivCompanyLogo);

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(getAdapterPosition());
                }
            });
        }
    }
}
