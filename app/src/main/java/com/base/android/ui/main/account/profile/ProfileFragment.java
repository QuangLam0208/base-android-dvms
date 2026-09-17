package com.base.android.ui.main.account.profile;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;

import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.base.android.utils.WebViewUtils;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.base.android.BR;
import com.base.android.R;
import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.databinding.FragmentProfileBinding;
import com.base.android.databinding.LayoutBottomSheetLanguageBinding;
import com.base.android.databinding.LayoutBottomSheetThemeBinding;
import com.base.android.databinding.LayoutBottomSheetWebviewDemoBinding;
import com.base.android.databinding.LayoutDialogInputUrlBinding;
import com.base.android.di.component.FragmentComponent;
import com.base.android.helper.LocaleHelper;
import com.base.android.helper.ThemeHelper;
import com.base.android.ui.base.fragment.BaseFragment;
import com.base.android.ui.main.MainActivity;
import com.base.android.ui.main.account.login.LoginActivity;
import com.base.android.ui.main.qrscan.QRScanActivity;
import com.base.android.utils.ImagePickerUtils;
import com.base.android.utils.ImageStorageUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

public class ProfileFragment extends BaseFragment<FragmentProfileBinding, ProfileViewModel> {

    private final ImagePickerUtils imagePickerUtils = new ImagePickerUtils(this, new ImagePickerUtils.ImagePickerCallback() {
        @Override
        public void onImagePicked(Uri uri) {
            if (uri == null || getContext() == null) return;
            // tạo file tạm để lưu ảnh sau khi crop
            File destFile = new File(requireContext().getFilesDir(), "user_avatar.jpg");
            // lưu ảnh vào file tạm
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
        viewModel.updateLanguageDisplay();
        viewModel.updateThemeDisplay();
        boolean isDark = ThemeHelper.isDarkMode(requireContext(), viewModel.getTheme());
        viewModel.setNightMode(isDark);
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
        if (getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) return;
        String currentLang = viewModel.getLanguage();
        boolean isEn = LocaleHelper.LANGUAGE_EN.equals(currentLang);

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme);
        LayoutBottomSheetLanguageBinding sheetBinding = LayoutBottomSheetLanguageBinding.inflate(
                LayoutInflater.from(requireContext()), null, false);
        sheetBinding.setVm(viewModel);
        sheetBinding.setLifecycleOwner(getViewLifecycleOwner());
        sheetBinding.executePendingBindings();
        dialog.setContentView(sheetBinding.getRoot());

        sheetBinding.ivCheckVi.setImageResource(isEn ? R.drawable.ic_circle_outline : R.drawable.ic_check_circle);
        sheetBinding.ivCheckEn.setImageResource(isEn ? R.drawable.ic_check_circle : R.drawable.ic_circle_outline);

        sheetBinding.btnLangVi.setOnClickListener(v -> {
            dialog.dismiss();
            if (isEn) {
                changeLanguage(LocaleHelper.LANGUAGE_VI);
            }
        });

        sheetBinding.btnLangEn.setOnClickListener(v -> {
            dialog.dismiss();
            if (!isEn) {
                changeLanguage(LocaleHelper.LANGUAGE_EN);
            }
        });

        sheetBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

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
        viewModel.updateLanguageDisplay();
    }

    public void onThemeClick() {
        if (getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) return;
        String currentTheme = viewModel.getTheme();

        boolean isCurrentDark = ThemeHelper.isDarkMode(requireContext(), currentTheme);
        viewModel.setNightMode(isCurrentDark);

        // Khởi tạo BottomSheetDialog với requireContext() để có Window Token hợp lệ (tránh BadTokenException)
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme);

        LayoutBottomSheetThemeBinding sheetBinding = LayoutBottomSheetThemeBinding.inflate(
                LayoutInflater.from(requireContext()), null, false);
        sheetBinding.setVm(viewModel);
        sheetBinding.setLifecycleOwner(getViewLifecycleOwner());
        dialog.setContentView(sheetBinding.getRoot());

        // Hiển thị trạng thái checkmark hiện tại
        updateCheckIcons(currentTheme, sheetBinding.ivCheckDark, sheetBinding.ivCheckLight, sheetBinding.ivCheckSystem);

        sheetBinding.executePendingBindings();

        // Khi người dùng click vào các lựa chọn: KHÔNG đóng dialog, đổi theme tức thì!
        sheetBinding.btnThemeDark.setOnClickListener(v ->
                applySelectedTheme(PreferencesService.THEME_MODE_DARK, sheetBinding)
        );
        sheetBinding.btnThemeLight.setOnClickListener(v ->
                applySelectedTheme(PreferencesService.THEME_MODE_LIGHT, sheetBinding)
        );
        sheetBinding.btnThemeSystem.setOnClickListener(v ->
                applySelectedTheme(PreferencesService.THEME_MODE_SYSTEM, sheetBinding)
        );
        sheetBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void applySelectedTheme(String selectedThemeMode, LayoutBottomSheetThemeBinding sheetBinding) {
        if (getContext() == null || getActivity() == null) return;

        // 1. Checkmark đổi
        updateCheckIcons(selectedThemeMode, sheetBinding.ivCheckDark, sheetBinding.ivCheckLight, sheetBinding.ivCheckSystem);

        // 2. Save Preferences
        viewModel.setTheme(selectedThemeMode);

        // 3. Áp dụng setDefaultNightMode
        ThemeHelper.applyTheme(selectedThemeMode);

        // 4. Xác định isDark thực tế
        boolean isDark = ThemeHelper.isDarkMode(requireContext(), selectedThemeMode);

        // 5. Cập nhật ViewModel -> DataBinding lập tức đổi màu ProfileFragment VÀ BottomSheet đang mở
        viewModel.setNightMode(isDark);
        sheetBinding.executePendingBindings();

        // 6. Cập nhật text hiển thị ngôn ngữ/theme qua DataBinding
        viewModel.updateThemeDisplay();

        // 7. Gọi MainActivity đổi màu Status Bar và Navigation Bar
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.applyThemeColors(isDark);
        }
    }

    private void updateCheckIcons(String themeMode, ImageView ivDark, ImageView ivLight, ImageView ivSystem) {
        ivDark.setImageResource(PreferencesService.THEME_MODE_DARK.equals(themeMode)
                ? R.drawable.ic_check_circle : R.drawable.ic_circle_outline);
        ivLight.setImageResource(PreferencesService.THEME_MODE_LIGHT.equals(themeMode)
                ? R.drawable.ic_check_circle : R.drawable.ic_circle_outline);
        ivSystem.setImageResource(PreferencesService.THEME_MODE_SYSTEM.equals(themeMode)
                ? R.drawable.ic_check_circle : R.drawable.ic_circle_outline);
    }

    public void updateThemeDisplay() {
        viewModel.updateThemeDisplay();
    }

    public void onWebViewDemoClick() {
        if (getContext() == null || getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme);
        LayoutBottomSheetWebviewDemoBinding sheetBinding = LayoutBottomSheetWebviewDemoBinding.inflate(
                LayoutInflater.from(requireContext()), null, false);
        sheetBinding.setVm(viewModel);
        sheetBinding.setLifecycleOwner(getViewLifecycleOwner());
        sheetBinding.executePendingBindings();
        dialog.setContentView(sheetBinding.getRoot());

        // Báo Tuổi Trẻ
        sheetBinding.btnDemoTuoitre.setOnClickListener(v -> {
            dialog.dismiss();
            WebViewUtils.openTuoitre(requireContext());
        });

        // Facebook
        sheetBinding.btnDemoFacebook.setOnClickListener(v -> {
            dialog.dismiss();
            WebViewUtils.openFacebook(requireContext());
        });

        // Nhập URL bất kỳ
        sheetBinding.btnDemoCustomUrl.setOnClickListener(v -> {
            dialog.dismiss();
            showCustomUrlDialog();
        });

        // Hủy
        sheetBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showCustomUrlDialog() {
        if (getContext() == null || getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) return;
        boolean isDark = ThemeHelper.isDarkMode(requireContext(), viewModel.getTheme());
        LayoutDialogInputUrlBinding dialogBinding = LayoutDialogInputUrlBinding.inflate(
                LayoutInflater.from(requireContext()), null, false);
        dialogBinding.setIsNightMode(isDark);
        dialogBinding.etUrl.setText("https://");
        dialogBinding.etUrl.setSelection(dialogBinding.etUrl.getText().length());
        dialogBinding.executePendingBindings();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.webview_input_url_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Mở", (d, which) -> {
                    String url = dialogBinding.etUrl.getText().toString().trim();
                    if (!url.isEmpty() && !url.equalsIgnoreCase("https://")) {
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://" + url;
                        }
                        WebViewUtils.openUrl(requireContext(), url);
                    } else {
                        viewModel.showErrorMessage(getString(R.string.webview_input_url_empty));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void handleLogout() {
        viewModel.logout();
        viewModel.showSuccessMessage(getString(R.string.logout_success));
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finishAffinity();
    }

    @Override
    protected void onThemeChanged(boolean isDark) {
        viewModel.updateThemeDisplay();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadSavedAvatar();
            viewModel.updateLanguageDisplay();
            viewModel.updateThemeDisplay();
            boolean isDark = ThemeHelper.isDarkMode(requireContext(), viewModel.getTheme());
            viewModel.setNightMode(isDark);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String currentTheme = viewModel.getTheme();
        if (PreferencesService.THEME_MODE_SYSTEM.equals(currentTheme)) {
            boolean isDark = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            ThemeHelper.setSystemNightMode(isDark);
            viewModel.setNightMode(isDark);
            viewModel.updateThemeDisplay();
        }
    }
}
