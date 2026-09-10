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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.base.android.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private final ActivityResultLauncher<String> cameraPermissionLauncher;
    private final ActivityResultLauncher<String> storagePermissionLauncher;
    private final ActivityResultLauncher<Uri> takePictureLauncher;
    private final ActivityResultLauncher<String> galleryLauncher;

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
                        String msg = (ctx != null)
                                ? ctx.getString(R.string.permission_camera_required)
                                : "Camera permission required";
                        callback.onError(msg);
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
                        String msg = (ctx != null)
                                ? ctx.getString(R.string.permission_storage_required)
                                : "Storage permission required";
                        callback.onError(msg);
                    }
                }
        );

        this.takePictureLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                isSuccess -> {
                    if (Boolean.TRUE.equals(isSuccess) && cameraPhotoUri != null) {
                        callback.onImagePicked(cameraPhotoUri);
                    }
                }
        );

        this.galleryLauncher = caller.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        callback.onImagePicked(uri);
                    }
                }
        );
    }

    @Nullable
    public Context getContext() {
        if (fragment != null) {
            return fragment.getContext();
        }
        return activity;
    }

    public void showImagePickerDialog() {
        Context ctx = getContext();
        if (ctx == null) return;
        showImagePickerDialog(ctx, this::checkCameraPermissionAndOpen, this::checkStoragePermissionAndOpen);
    }

    public void checkCameraPermissionAndOpen() {
        Context ctx = getContext();
        if (ctx == null) return;
        if (hasCameraPermission(ctx)) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    public void checkStoragePermissionAndOpen() {
        Context ctx = getContext();
        if (ctx == null) return;
        if (hasStoragePermission(ctx)) {
            openGallery();
        } else {
            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    public void openCamera() {
        Context ctx = getContext();
        if (ctx == null) return;
        try {
            File photoFile = createTempImageFile(ctx);
            cameraPhotoUri = getUriForFile(ctx, photoFile);
            takePictureLauncher.launch(cameraPhotoUri);
        } catch (Exception e) {
            Timber.e(e, "Error opening camera");
            callback.onError(ctx.getString(R.string.error_open_camera));
        }
    }

    public void openGallery() {
        galleryLauncher.launch("image/*");
    }

    // ==================== Static Helper Methods ====================

    public static File createTempImageFile(@NonNull Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "AVATAR_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            storageDir = context.getCacheDir();
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public static Uri getUriForFile(@NonNull Context context, @NonNull File file) {
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                file
        );
    }

    public static boolean hasCameraPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasStoragePermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static AlertDialog showImagePickerDialog(@NonNull Context context,
                                                    @NonNull Runnable onCameraSelected,
                                                    @NonNull Runnable onGallerySelected) {
        String[] options = new String[]{
                context.getString(R.string.choose_from_camera),
                context.getString(R.string.choose_from_gallery)
        };

        return new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.choose_avatar_title))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        onCameraSelected.run();
                    } else if (which == 1) {
                        onGallerySelected.run();
                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show();
    }
}
