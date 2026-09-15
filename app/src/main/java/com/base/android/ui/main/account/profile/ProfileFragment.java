package com.base.android.ui.main.account.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import android.view.LayoutInflater;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.base.android.BR;
import com.base.android.R;
import com.base.android.databinding.FragmentProfileBinding;
import com.base.android.di.component.FragmentComponent;
import com.base.android.helper.LocaleHelper;
import com.base.android.ui.base.fragment.BaseFragment;
import com.base.android.ui.main.MainActivity;
import com.base.android.ui.main.account.login.LoginActivity;
import com.base.android.ui.main.qrscan.QRScanActivity;
import com.base.android.utils.ImagePickerUtils;

import java.io.File;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding, ProfileViewModel> {

    private final ImagePickerUtils imagePickerUtils = new ImagePickerUtils(this, new ImagePickerUtils.ImagePickerCallback() {
        @Override
        public void onImagePicked(Uri uri) {
            if (uri == null) return;
            String savedPath = saveAvatarToInternalStorage(uri);
            if (savedPath == null) return;
            viewModel.saveAvatarUri(savedPath);
            // pass File object so Glide loads correctly and bypasses stale cache
            displayAvatar(new File(savedPath));
        }

        @Override
        public void onError(String errorMessage) {
            viewModel.showErrorMessage(errorMessage);
        }
    });

    private String saveAvatarToInternalStorage(Uri uri) {
        if (getContext() == null || uri == null) return null;
        try {
            java.io.InputStream is = requireContext().getContentResolver().openInputStream(uri);
            if (is == null) return null;
            java.io.File dest = new java.io.File(requireContext().getFilesDir(), "user_avatar.jpg");
            java.io.OutputStream os = new java.io.FileOutputStream(dest);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            return dest.getAbsolutePath();
        } catch (Exception e) {
            timber.log.Timber.e(e, "Error saving avatar to internal storage");
            return null;
        }
    }

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
            // use File object instead of Uri.parse() which produced a scheme-less
            // URI that Glide couldn't resolve, causing the avatar to always appear as default.
            File avatarFile = new File(savedAvatar);
            if (avatarFile.exists()) {
                displayAvatar(avatarFile);
            }
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

    public void onQRScanClick() {
        Intent intent = new Intent(requireActivity(), QRScanActivity.class);
        startActivity(intent);
    }

    private void displayAvatar(File file) {
        if (file == null || !file.exists() || getContext() == null) return;
        binding.ivAvatar.setPadding(0, 0, 0, 0);
        binding.ivAvatar.setImageTintList(null);
        // disable Glide disk+memory cache so overwritten user_avatar.jpg is
        // always read fresh — without this, Glide serves the old cached version even after
        // the file has been replaced with the new gallery/camera image.
        Glide.with(this)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .circleCrop()
                .into(binding.ivAvatar);
    }

    public void onLanguageClick() {
        String currentLang = viewModel.getLanguage();
        boolean isEn = LocaleHelper.LANGUAGE_EN.equals(currentLang);

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_bottom_sheet_language, null);
        dialog.setContentView(sheetView);

        ImageView ivCheckVi = sheetView.findViewById(R.id.iv_check_vi);
        ImageView ivCheckEn = sheetView.findViewById(R.id.iv_check_en);

        ivCheckVi.setImageResource(isEn ? R.drawable.ic_circle_outline : R.drawable.ic_check_circle);
        ivCheckEn.setImageResource(isEn ? R.drawable.ic_check_circle : R.drawable.ic_circle_outline);

        sheetView.findViewById(R.id.btn_lang_vi).setOnClickListener(v -> {
            dialog.dismiss();
            if (isEn) {
                changeLanguage(LocaleHelper.LANGUAGE_VI);
            }
        });

        sheetView.findViewById(R.id.btn_lang_en).setOnClickListener(v -> {
            dialog.dismiss();
            if (!isEn) {
                changeLanguage(LocaleHelper.LANGUAGE_EN);
            }
        });

        sheetView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void changeLanguage(String langCode) {
        viewModel.setLanguage(langCode);
        LocaleHelper.setLocale(requireContext(), langCode);

        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.putExtra(MainActivity.KEY_CURRENT_TAG, MainActivity.TAG_PROFILE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
        startActivity(intent);
        requireActivity().finishAffinity();
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
