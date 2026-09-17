package com.base.android.ui.main.account.profile;

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
        updateLanguageDisplay();
        updateThemeDisplay();
        boolean isDark = ThemeHelper.isDarkMode(requireContext(), viewModel.getTheme());
        applyProfileThemeColors(isDark);
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

        // Hiển thị trạng thái checkmark hiện tại
        updateCheckIcons(currentTheme, ivCheckDark, ivCheckLight, ivCheckSystem);

        // Hiển thị màu sắc Bottom Sheet theo theme hiện tại
        boolean isCurrentDark = ThemeHelper.isDarkMode(requireContext(), currentTheme);
        updateBottomSheetColors(sheetView, isCurrentDark);

        // Khi người dùng click vào các lựa chọn: KHÔNG đóng dialog, đổi theme tức thì!
        sheetView.findViewById(R.id.btn_theme_dark).setOnClickListener(v ->
                applySelectedTheme(PreferencesService.THEME_MODE_DARK, sheetView, ivCheckDark, ivCheckLight, ivCheckSystem)
        );

        sheetView.findViewById(R.id.btn_theme_light).setOnClickListener(v ->
                applySelectedTheme(PreferencesService.THEME_MODE_LIGHT, sheetView, ivCheckDark, ivCheckLight, ivCheckSystem)
        );

        sheetView.findViewById(R.id.btn_theme_system).setOnClickListener(v ->
                applySelectedTheme(PreferencesService.THEME_MODE_SYSTEM, sheetView, ivCheckDark, ivCheckLight, ivCheckSystem)
        );

        sheetView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void applySelectedTheme(String selectedThemeMode, View sheetView,
                                    ImageView ivDark, ImageView ivLight, ImageView ivSystem) {
        if (getContext() == null || getActivity() == null) return;

        // 1. Checkmark đổi
        updateCheckIcons(selectedThemeMode, ivDark, ivLight, ivSystem);

        // 2. Save Preferences
        viewModel.setTheme(selectedThemeMode);

        // 3. Áp dụng setDefaultNightMode
        ThemeHelper.applyTheme(selectedThemeMode);

        // 4. Xác định isDark thực tế
        boolean isDark = ThemeHelper.isDarkMode(requireContext(), selectedThemeMode);

        // 5. BottomSheet đổi màu
        updateBottomSheetColors(sheetView, isDark);

        // 6. Profile đổi màu
        applyProfileThemeColors(isDark);
        updateThemeDisplay();

        // 7. MainActivity đổi system UI
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.applyThemeColors(isDark);
            mainActivity.notifyThemeChanged();
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

    private void updateBottomSheetColors(View sheetView, boolean isDark) {
        if (sheetView == null) return;

        int sheetBg = ThemeHelper.getBottomSheetBackgroundColor(isDark);
        int textPrimary = ThemeHelper.getTextPrimaryColor(isDark);
        int dividerColor = ThemeHelper.getBottomSheetDividerColor(isDark);
        int textCancel = ThemeHelper.getTextCancelColor(isDark);

        sheetView.setBackgroundTintList(ColorStateList.valueOf(sheetBg));

        TextView tvTitle = sheetView.findViewById(R.id.tv_bottom_sheet_theme_title);
        if (tvTitle != null) tvTitle.setTextColor(textPrimary);

        TextView tvDark = sheetView.findViewById(R.id.tv_theme_dark_label);
        if (tvDark != null) tvDark.setTextColor(textPrimary);

        TextView tvLight = sheetView.findViewById(R.id.tv_theme_light_label);
        if (tvLight != null) tvLight.setTextColor(textPrimary);

        TextView tvSystem = sheetView.findViewById(R.id.tv_theme_system_label);
        if (tvSystem != null) tvSystem.setTextColor(textPrimary);

        TextView tvCancel = sheetView.findViewById(R.id.tv_cancel_label);
        if (tvCancel != null) tvCancel.setTextColor(textCancel);

        View dDark = sheetView.findViewById(R.id.divider_dark);
        if (dDark != null) dDark.setBackgroundColor(dividerColor);

        View dLight = sheetView.findViewById(R.id.divider_light);
        if (dLight != null) dLight.setBackgroundColor(dividerColor);

        View dSystem = sheetView.findViewById(R.id.divider_system);
        if (dSystem != null) dSystem.setBackgroundColor(dividerColor);
    }

    public void applyProfileThemeColors(boolean isDark) {
        if (binding == null || getContext() == null) return;

        int screenBg = ThemeHelper.getScreenBackgroundColor(isDark);
        int cardBg = ThemeHelper.getCardBackgroundColor(isDark);
        int textPrimary = ThemeHelper.getTextPrimaryColor(isDark);
        int dividerColor = ThemeHelper.getDividerColor(isDark);

        // Root background
        binding.getRoot().setBackgroundColor(screenBg);

        // Header Title
        binding.tvTitle.setTextColor(textPrimary);

        // Cards background
        binding.cardUserInfo.setCardBackgroundColor(cardBg);
        binding.cardSettings.setCardBackgroundColor(cardBg);

        // Username text
        binding.tvUsername.setTextColor(textPrimary);

        // Settings items: Row icons and labels
        updateRowTheme(binding.layoutLanguage, textPrimary);
        updateRowTheme(binding.layoutQrScan, textPrimary);
        updateRowTheme(binding.layoutTheme, textPrimary);
        updateRowTheme(binding.layoutWebviewDemo, textPrimary);

        // Update divider views inside layoutSettings
        for (int i = 0; i < binding.layoutSettings.getChildCount(); i++) {
            View child = binding.layoutSettings.getChildAt(i);
            if (!(child instanceof android.widget.LinearLayout)) {
                child.setBackgroundColor(dividerColor);
            }
        }
    }

    private void updateRowTheme(android.widget.LinearLayout rowLayout, int textPrimary) {
        if (rowLayout == null) return;
        View icon = rowLayout.getChildAt(0);
        if (icon instanceof ImageView) {
            ((ImageView) icon).setImageTintList(ColorStateList.valueOf(textPrimary));
        }
        View label = rowLayout.getChildAt(1);
        if (label instanceof TextView) {
            ((TextView) label).setTextColor(textPrimary);
        }
    }

    public void updateThemeDisplay() {
        String currentTheme = viewModel.getTheme();
        if (PreferencesService.THEME_MODE_LIGHT.equals(currentTheme)) {
            binding.tvCurrentTheme.setText(R.string.theme_light);
        } else if (PreferencesService.THEME_MODE_SYSTEM.equals(currentTheme)) {
            binding.tvCurrentTheme.setText(R.string.theme_system);
        } else {
            binding.tvCurrentTheme.setText(R.string.theme_dark);
        }
    }

    public void onWebViewDemoClick() {
        if (getContext() == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.AppBottomSheetDialogTheme);
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.layout_bottom_sheet_webview_demo, null);
        dialog.setContentView(sheetView);

        // Báo Tuổi Trẻ
        sheetView.findViewById(R.id.btn_demo_tuoitre).setOnClickListener(v -> {
            dialog.dismiss();
            WebViewUtils.openTuoitre(requireContext());
        });

        // Facebook
        sheetView.findViewById(R.id.btn_demo_facebook).setOnClickListener(v -> {
            dialog.dismiss();
            WebViewUtils.openFacebook(requireContext());
        });

        // Nhập URL bất kỳ
        sheetView.findViewById(R.id.btn_demo_custom_url).setOnClickListener(v -> {
            dialog.dismiss();
            showCustomUrlDialog();
        });

        // Hủy
        sheetView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showCustomUrlDialog() {
        if (getContext() == null) return;
        EditText input = new EditText(requireContext());
        input.setHint(R.string.webview_input_url_hint);
        input.setText("https://");
        input.setSelection(input.getText().length());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        FrameLayout container = new FrameLayout(requireContext());
        int paddingHorizontal = getResources().getDimensionPixelSize(R.dimen._20sdp);
        int paddingTop = getResources().getDimensionPixelSize(R.dimen._10sdp);
        container.setPadding(paddingHorizontal, paddingTop, paddingHorizontal, 0);
        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.webview_input_url_title)
                .setView(container)
                .setPositiveButton("Mở", (d, which) -> {
                    String url = input.getText().toString().trim();
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
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadSavedAvatar();
            updateLanguageDisplay();
            updateThemeDisplay();
            boolean isDark = ThemeHelper.isDarkMode(requireContext(), viewModel.getTheme());
            applyProfileThemeColors(isDark);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        String currentTheme = viewModel.getTheme();
        if (PreferencesService.THEME_MODE_SYSTEM.equals(currentTheme)) {
            boolean isDark = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            applyProfileThemeColors(isDark);
            updateThemeDisplay();
        }
    }
}
