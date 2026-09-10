package com.base.android.ui.main.account.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.base.android.BR;
import com.base.android.R;
import com.base.android.databinding.FragmentProfileBinding;
import com.base.android.di.component.FragmentComponent;
import com.base.android.helper.LocaleHelper;
import com.base.android.ui.base.fragment.BaseFragment;
import com.base.android.ui.main.MainActivity;
import com.base.android.ui.main.account.login.LoginActivity;
import com.base.android.utils.ImagePickerUtils;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding, ProfileViewModel> {

    private final ImagePickerUtils imagePickerUtils = new ImagePickerUtils(this, new ImagePickerUtils.ImagePickerCallback() {
        @Override
        public void onImagePicked(Uri uri) {
            if (uri == null) return;
            viewModel.saveAvatarUri(uri.toString());
            displayAvatar(uri);
        }

        @Override
        public void onError(String errorMessage) {
            viewModel.showErrorMessage(errorMessage);
        }
    });

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadSavedAvatar();
        updateLanguageDisplay();
    }

    private void loadSavedAvatar() {
        String savedAvatar = viewModel.getSavedAvatarUri();
        if (savedAvatar != null && !savedAvatar.trim().isEmpty()) {
            displayAvatar(Uri.parse(savedAvatar));
        }
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile;
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

    public void onAvatarClick() {
        imagePickerUtils.showImagePickerDialog();
    }

    private void displayAvatar(Object source) {
        if (source == null || getContext() == null) return;
        binding.ivAvatar.setPadding(0, 0, 0, 0);
        binding.ivAvatar.setImageTintList(null);
        Glide.with(this)
                .load(source)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.ivAvatar);
    }

    public void onLanguageClick() {
        String currentLang = viewModel.getLanguage();
        int checkedItem = LocaleHelper.LANGUAGE_EN.equals(currentLang) ? 1 : 0;

        String[] options = new String[]{
                getString(R.string.language_vi),
                getString(R.string.language_en)
        };

        final int[] selectedIndex = {checkedItem};

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.choose_language_title))
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                    String newLang = (selectedIndex[0] == 1) ? LocaleHelper.LANGUAGE_EN : LocaleHelper.LANGUAGE_VI;
                    if (!newLang.equals(currentLang)) {
                        changeLanguage(newLang);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void changeLanguage(String langCode) {
        viewModel.setLanguage(langCode);
        LocaleHelper.setLocale(requireContext(), langCode);

        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.putExtra(MainActivity.KEY_CURRENT_TAG, MainActivity.TAG_PROFILE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void updateLanguageDisplay() {
        String currentLang = viewModel.getLanguage();
        if (LocaleHelper.LANGUAGE_EN.equals(currentLang)) {
            binding.tvCurrentLanguage.setText(R.string.language_en);
        } else {
            binding.tvCurrentLanguage.setText(R.string.language_vi);
        }
    }

    public void handleLogout() {
        viewModel.logout();
        viewModel.showSuccessMessage(getString(R.string.logout_success));
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadSavedAvatar();
            updateLanguageDisplay();
        }
    }
}
