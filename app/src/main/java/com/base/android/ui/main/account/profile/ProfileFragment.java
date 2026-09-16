package com.base.android.ui.main.account.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import android.view.LayoutInflater;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.base.android.BR;
import com.base.android.R;
import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.databinding.FragmentProfileBinding;
import com.base.android.di.component.FragmentComponent;
import com.base.android.helper.LocaleHelper;
import com.base.android.helper.ThemeHelper;
import com.base.android.ui.base.fragment.BaseFragment;
import com.base.android.ui.main.MainActivity;
import com.base.android.ui.main.account.login.LoginActivity;
import com.base.android.ui.main.qrscan.QRScanActivity;
import com.base.android.utils.ImagePickerUtils;
import com.base.android.utils.ImageStorageUtils;

import java.io.File;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding, ProfileViewModel> {

    private final ImagePickerUtils imagePickerUtils = new ImagePickerUtils(this, new ImagePickerUtils.ImagePickerCallback() {
        @Override
        public void onImagePicked(Uri uri) {
            if (uri == null || getContext() == null) return;
            File destFile = new File(requireContext().getFilesDir(), "user_avatar.jpg");
            ImageStorageUtils.saveImageAsync(requireContext(), uri, destFile, 512, new ImageStorageUtils.SaveCallback() {
                @Override
                public void onSuccess(File savedFile) {
                    viewModel.saveAvatarUri(savedFile.getAbsolutePath());
                    displayAvatar(savedFile);
                }

                @Override
                public void onError(Throwable throwable) {
                    viewModel.showErrorMessage(throwable.getMessage());
                }
            });
        }

        @Override
        public void onError(String errorMessage) {
            viewModel.showErrorMessage(errorMessage);
        }
    }).setCropCircle(true); // Bật crop hình tròn cho Avatar

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadSavedAvatar();
        updateLanguageDisplay();
        updateThemeDisplay();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedAvatar();
    }

    private void loadSavedAvatar() {
        String savedAvatar = viewModel.getSavedAvatarUri();
        File avatarFile = null;

        if (savedAvatar != null && !savedAvatar.trim().isEmpty()) {
            File file = new File(savedAvatar);
            if (file.exists()) {
                if (file.length() > 0) { // Chỉ nhận file có dữ liệu
                    avatarFile = file;
                } else {
                    file.delete(); // Xóa file rỗng nếu bị lỗi
                }
            }
        }

        if (avatarFile == null && getContext() != null) {
            File fallbackFile = new File(requireContext().getFilesDir(), "user_avatar.jpg");
            if (fallbackFile.exists()) {
                if (fallbackFile.length() > 0) {
                    avatarFile = fallbackFile;
                    viewModel.saveAvatarUri(fallbackFile.getAbsolutePath());
                } else {
                    fallbackFile.delete(); // Xóa file rỗng
                }
            }
        }
        if (avatarFile != null && avatarFile.exists() && avatarFile.length() > 0) {
            displayAvatar(avatarFile);
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

        Glide.with(this)
                .load(file)
                .signature(new ObjectKey(file.lastModified()))
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

    public void onThemeClick() {
        String currentTheme = viewModel.getTheme();

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_bottom_sheet_theme, null);
        dialog.setContentView(sheetView);

        ImageView ivCheckDark = sheetView.findViewById(R.id.iv_check_dark);
        ImageView ivCheckLight = sheetView.findViewById(R.id.iv_check_light);
        ImageView ivCheckSystem = sheetView.findViewById(R.id.iv_check_system);

        ivCheckDark.setImageResource(PreferencesService.THEME_MODE_DARK.equals(currentTheme) ? R.drawable.ic_check_circle : R.drawable.ic_circle_outline);
        ivCheckLight.setImageResource(PreferencesService.THEME_MODE_LIGHT.equals(currentTheme) ? R.drawable.ic_check_circle : R.drawable.ic_circle_outline);
        ivCheckSystem.setImageResource(PreferencesService.THEME_MODE_SYSTEM.equals(currentTheme) ? R.drawable.ic_check_circle : R.drawable.ic_circle_outline);

        sheetView.findViewById(R.id.btn_theme_dark).setOnClickListener(v -> {
            dialog.dismiss();
            if (!PreferencesService.THEME_MODE_DARK.equals(currentTheme)) {
                changeTheme(PreferencesService.THEME_MODE_DARK);
            }
        });

        sheetView.findViewById(R.id.btn_theme_light).setOnClickListener(v -> {
            dialog.dismiss();
            if (!PreferencesService.THEME_MODE_LIGHT.equals(currentTheme)) {
                changeTheme(PreferencesService.THEME_MODE_LIGHT);
            }
        });

        sheetView.findViewById(R.id.btn_theme_system).setOnClickListener(v -> {
            dialog.dismiss();
            if (!PreferencesService.THEME_MODE_SYSTEM.equals(currentTheme)) {
                changeTheme(PreferencesService.THEME_MODE_SYSTEM);
            }
        });

        sheetView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void changeTheme(String themeMode) {
        viewModel.setTheme(themeMode);
        ThemeHelper.applyTheme(themeMode);

        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.putExtra(MainActivity.KEY_CURRENT_TAG, MainActivity.TAG_PROFILE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateThemeDisplay() {
        String currentTheme = viewModel.getTheme();
        if (PreferencesService.THEME_MODE_LIGHT.equals(currentTheme)) {
            binding.tvCurrentTheme.setText(R.string.theme_light);
        } else if (PreferencesService.THEME_MODE_SYSTEM.equals(currentTheme)) {
            binding.tvCurrentTheme.setText(R.string.theme_system);
        } else {
            binding.tvCurrentTheme.setText(R.string.theme_dark);
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
            updateThemeDisplay();
        }
    }
}
