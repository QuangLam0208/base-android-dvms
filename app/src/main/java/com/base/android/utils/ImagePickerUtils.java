package com.base.android.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.base.android.R;
import com.base.android.databinding.LayoutBottomSheetAvatarBinding;
import com.base.android.helper.ThemeHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;

import com.yalantis.ucrop.UCrop;

import timber.log.Timber;

public class ImagePickerUtils {

    public interface ImagePickerCallback {
        void onImagePicked(Uri uri);
        default void onError(String errorMessage) {}
    }

    private final Fragment fragment;
    private final ComponentActivity activity;
    private final ImagePickerCallback callback;

    private Uri cameraPhotoUri;
    private Uri currentTempSourceUri;
    private final ActivityResultLauncher<String> cameraPermissionLauncher;
    private final ActivityResultLauncher<String> storagePermissionLauncher;
    private final ActivityResultLauncher<Uri> takePictureLauncher;
    private final ActivityResultLauncher<String> galleryLauncher;
    private final ActivityResultLauncher<Intent> cropLauncher;

    // Crop configurations (mặc định tắt crop)
    private boolean cropEnabled = false;
    private boolean cropCircle = false;
    private float aspectRatioX = 1f;
    private float aspectRatioY = 1f;
    private int maxResultWidth = 512;
    private int maxResultHeight = 512;

    public ImagePickerUtils(@NonNull Fragment fragment, @NonNull ImagePickerCallback callback) {
        this(fragment, null, fragment, callback);
    }

    public ImagePickerUtils(@NonNull ComponentActivity activity, @NonNull ImagePickerCallback callback) {
        this(null, activity, activity, callback);
    }

    private ImagePickerUtils(@Nullable Fragment fragment,
                             @Nullable ComponentActivity activity,
                             @NonNull ActivityResultCaller caller,
                             @NonNull ImagePickerCallback callback) {
        this.fragment = fragment;
        this.activity = activity;
        this.callback = callback;

        this.cameraPermissionLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (Boolean.TRUE.equals(isGranted)) {
                        openCamera();
                    } else {
                        Context ctx = getContext();
                        Activity act = getActivity();
                        if (act != null && PermissionUtils.isPermanentlyDenied(act, Manifest.permission.CAMERA)) {
                            PermissionUtils.showPermissionSettingsDialog(act, ctx != null ? ctx.getString(R.string.permission_camera_rationale) : null);
                        } else {
                            String msg = (ctx != null)
                                    ? ctx.getString(R.string.permission_camera_required)
                                    : "Camera permission required";
                            callback.onError(msg);
                        }
                    }
                }
        );

        this.storagePermissionLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (Boolean.TRUE.equals(isGranted)) {
                        openGallery();
                    } else {
                        Context ctx = getContext();
                        Activity act = getActivity();
                        String storagePerm = PermissionUtils.getStoragePermissionName();
                        if (act != null && PermissionUtils.isPermanentlyDenied(act, storagePerm)) {
                            PermissionUtils.showPermissionSettingsDialog(act, ctx != null ? ctx.getString(R.string.permission_storage_rationale) : null);
                        } else {
                            String msg = (ctx != null)
                                    ? ctx.getString(R.string.permission_storage_required)
                                    : "Storage permission required";
                            callback.onError(msg);
                        }
                    }
                }
        );

        this.takePictureLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (Boolean.TRUE.equals(isSuccess) && cameraPhotoUri != null) {
                        handleImagePicked(cameraPhotoUri);
                    }
                }
        );

        this.galleryLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleImagePicked(uri);
                    }
                }
        );

        this.cropLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    cleanupTempSource();
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri croppedUri = UCrop.getOutput(result.getData());
                        if (croppedUri != null) {
                            callback.onImagePicked(croppedUri); // callback sau khi crop xong
                        }
                    } else if (result.getResultCode() == UCrop.RESULT_ERROR && result.getData() != null) {
                        Throwable cropError = UCrop.getError(result.getData());
                        if (cropError != null) {
                            Timber.e(cropError, "Error in uCrop");
                            callback.onError(cropError.getMessage());
                        }
                    }
                }
        );
    }

    public ImagePickerUtils setCropEnabled(boolean enabled) {
        this.cropEnabled = enabled;
        return this;
    }

    public ImagePickerUtils setCropCircle(boolean circle) {
        this.cropCircle = circle;
        if (circle) this.cropEnabled = true;
        return this;
    }

    public ImagePickerUtils setAspectRatio(float x, float y) {
        this.aspectRatioX = x;
        this.aspectRatioY = y;
        this.cropEnabled = true;
        return this;
    }

    public ImagePickerUtils setMaxResultSize(int width, int height) {
        this.maxResultWidth = width;
        this.maxResultHeight = height;
        return this;
    }

    private void handleImagePicked(Uri uri) {
        if (uri == null) return;
        if (cropEnabled) {
            // avt nên thực hiện crop (cropCircle = true)
            startCrop(uri);
        } else {
            // không crop
            callback.onImagePicked(uri);
        }
    }

    private void startCrop(Uri sourceUri) {
        Context ctx = getContext();
        if (ctx == null) return;

        // Copy sang file tạm trong cache của app để tránh lỗi mất quyền đọc ContentProvider (EFAULT: Bad address)
        Uri safeSourceUri = copyUriToTempFile(ctx, sourceUri);
        this.currentTempSourceUri = safeSourceUri;

        // Tạo file tạm để lưu ảnh sau khi crop
        File cropDestination = new File(ctx.getCacheDir(), "crop_" + System.currentTimeMillis() + ".jpg");
        Uri destinationUri = Uri.fromFile(cropDestination);

        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(cropCircle);
        options.setShowCropGrid(!cropCircle);
        options.setCropFrameColor(ContextCompat.getColor(ctx, R.color.accent_color));
        options.setToolbarColor(ContextCompat.getColor(ctx, R.color.main_background));
        options.setStatusBarColor(ContextCompat.getColor(ctx, R.color.main_background));
        options.setToolbarWidgetColor(ContextCompat.getColor(ctx, R.color.white));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(ctx, R.color.accent_color));
        options.setCompressionQuality(85);
        options.setHideBottomControls(false);

        UCrop uCrop = UCrop.of(safeSourceUri, destinationUri)
                .withAspectRatio(aspectRatioX, aspectRatioY) // Giới hạn tỉ lệ crop 1:1
                .withMaxResultSize(maxResultWidth, maxResultHeight)
                .withOptions(options);

        Intent intent = uCrop.getIntent(ctx);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropLauncher.launch(intent); // Mở uCrop đã đăng ký ở trên: this.cropLauncher
    }

    private Uri copyUriToTempFile(Context ctx, Uri sourceUri) {
        if (sourceUri == null) return null;
        if ("file".equalsIgnoreCase(sourceUri.getScheme())) {
            return sourceUri;
        }
        try {
            File tempFile = new File(ctx.getCacheDir(), "raw_input_" + System.currentTimeMillis() + ".jpg");
            try (InputStream in = ctx.getContentResolver().openInputStream(sourceUri);
                 OutputStream out = new FileOutputStream(tempFile)) {
                if (in == null) return sourceUri;
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                out.flush();
            }
            return Uri.fromFile(tempFile);
        } catch (Exception e) {
            Timber.e(e, "copyUriToTempFile failed, falling back to sourceUri");
            return sourceUri;
        }
    }

    private void cleanupTempSource() {
        if (currentTempSourceUri != null && "file".equalsIgnoreCase(currentTempSourceUri.getScheme())) {
            try {
                String path = currentTempSourceUri.getPath();
                if (path != null) {
                    File file = new File(path);
                    if (file.exists() && file.getName().startsWith("raw_input_")) {
                        file.delete();
                    }
                }
            } catch (Exception ignored) {}
            currentTempSourceUri = null;
        }
    }

    @Nullable
    public Context getContext() {
        if (fragment != null) {
            return fragment.getContext();
        }
        return activity;
    }

    @Nullable
    public Activity getActivity() {
        if (fragment != null) {
            return fragment.getActivity();
        }
        return activity;
    }

    // 1. Hiện BottomSheet chọn Camera/Gallery
    public void showImagePickerDialog() {
        Context ctx = getContext();
        if (ctx == null) return;
        showImagePickerDialog(ctx, this::checkCameraPermissionAndOpen, this::checkStoragePermissionAndOpen);
    }

    public void checkCameraPermissionAndOpen() {
        Context ctx = getContext();
        if (ctx == null) return;
        if (PermissionUtils.hasCameraPermission(ctx)) {
            openCamera();
        } else {
            Activity act = getActivity();
            if (act != null && PermissionUtils.isPermanentlyDenied(act, Manifest.permission.CAMERA)) {
                PermissionUtils.showPermissionSettingsDialog(act, ctx.getString(R.string.permission_camera_rationale));
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }
    }

    public void checkStoragePermissionAndOpen() {
        Context ctx = getContext();
        if (ctx == null) return;
        if (PermissionUtils.hasStoragePermission(ctx)) {
            openGallery();
        } else {
            Activity act = getActivity();
            String storagePerm = PermissionUtils.getStoragePermissionName();
            if (act != null && PermissionUtils.isPermanentlyDenied(act, storagePerm)) {
                PermissionUtils.showPermissionSettingsDialog(act, ctx.getString(R.string.permission_storage_rationale));
            } else {
                storagePermissionLauncher.launch(storagePerm);
            }
        }
    }

    public void openCamera() {
        Context ctx = getContext();
        if (ctx == null) return;
        try {
            File photoFile = createTempImageFile(ctx);
            cameraPhotoUri = getUriForFile(ctx, photoFile);
            takePictureLauncher.launch(cameraPhotoUri); // Mở camera đã đăng ký ở trên: this.takePictureLauncher
        } catch (Exception e) {
            Timber.e(e, "Error opening camera");
            callback.onError(ctx.getString(R.string.error_open_camera));
        }
    }

    public void openGallery() {
        galleryLauncher.launch("image/*");
    }

    // ==================== Static Helper Methods ====================

    // Tạo file tạm và lưu vào thư mục riêng của app
    public static File createTempImageFile(@NonNull Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "AVATAR_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = context.getCacheDir();
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // Lấy Uri cho file tạm
    public static Uri getUriForFile(@NonNull Context context, @NonNull File file) {
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                file
        );
    }

    // 2. BottomSheet, gọi checkCameraPermissionAndOpen hoặc checkStoragePermissionAndOpen
    public static BottomSheetDialog showImagePickerDialog(@NonNull Context context,
                                                           @NonNull Runnable onCameraSelected,
                                                           @NonNull Runnable onGallerySelected) {
        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme);
        LayoutBottomSheetAvatarBinding sheetBinding = LayoutBottomSheetAvatarBinding.inflate(
                LayoutInflater.from(context), null, false);
        sheetBinding.setIsNightMode(ThemeHelper.isDarkMode(context));
        sheetBinding.executePendingBindings();
        dialog.setContentView(sheetBinding.getRoot());

        sheetBinding.btnCamera.setOnClickListener(v -> {
            dialog.dismiss();
            onCameraSelected.run();
        });

        sheetBinding.btnGallery.setOnClickListener(v -> {
            dialog.dismiss();
            onGallerySelected.run();
        });

        sheetBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        return dialog;
    }
}
