package com.base.android.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.base.android.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public final class PermissionUtils {

    private PermissionUtils() {
        // Utility class
    }

    /**
     * Danh sách tất cả các quyền cần thiết cho ứng dụng theo từng phiên bản Android.
     */
    @NonNull
    public static String[] getRequiredAppPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            // Android 12 trở xuống
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                // Android 9 trở xuống
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        return permissions.toArray(new String[0]);
    }

    /**
     * Lọc ra danh sách những quyền chưa được người dùng cấp phép.
     */
    @NonNull
    public static String[] getDeniedPermissions(@NonNull Context context) {
        List<String> deniedList = new ArrayList<>();
        if (!hasCameraPermission(context)) {
            deniedList.add(Manifest.permission.CAMERA);
        }
        if (!hasNotificationPermission(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                deniedList.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (!hasStoragePermission(context)) {
            if (Build.VERSION.SDK_INT >= 34) {
                deniedList.add(Manifest.permission.READ_MEDIA_IMAGES);
                deniedList.add("android.permission.READ_MEDIA_VISUAL_USER_SELECTED");
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                deniedList.add(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                deniedList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    deniedList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        }
        return deniedList.toArray(new String[0]);
    }

    /**
     * Kiểm tra một quyền cụ thể đã được cấp hay chưa.
     */
    public static boolean hasPermission(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Kiểm tra quyền Máy ảnh (Camera).
     */
    public static boolean hasCameraPermission(@NonNull Context context) {
        return hasPermission(context, Manifest.permission.CAMERA);
    }

    /**
     * Kiểm tra quyền Thông báo (Notification).
     */
    public static boolean hasNotificationPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return hasPermission(context, Manifest.permission.POST_NOTIFICATIONS);
        }
        return true;
    }

    /**
     * Lấy tên quyền truy cập ảnh tùy theo phiên bản Android.
     */
    @NonNull
    public static String getStoragePermissionName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        }
        return Manifest.permission.READ_EXTERNAL_STORAGE;
    }

    /**
     * Kiểm tra quyền Đọc ảnh / Bộ nhớ.
     */
    public static boolean hasStoragePermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= 34) {
            // Android 14+: người dùng có thể cấp full hoặc partial access
            return hasPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                    || hasPermission(context, "android.permission.READ_MEDIA_VISUAL_USER_SELECTED");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13
            return hasPermission(context, Manifest.permission.READ_MEDIA_IMAGES);
        }
        return hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    /**
     * Kiểm tra xem người dùng có chọn "Không hỏi lại" (Don't ask again) đối với quyền này không.
     */
    public static boolean isPermanentlyDenied(@NonNull Activity activity, @NonNull String permission) {
        if (hasPermission(activity, permission)) {
            return false;
        }
        // Nếu quyền bị từ chối và shouldShowRequestPermissionRationale == false -> Người dùng đã tick Don't ask again
        return !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Mở trang Cài đặt chi tiết của ứng dụng để người dùng bật lại quyền.
     */
    public static void openAppSettings(@NonNull Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Hiển thị Dialog hướng dẫn người dùng vào Cài đặt để cấp quyền khi bị từ chối vĩnh viễn.
     */
    public static void showPermissionSettingsDialog(@NonNull Activity activity, @Nullable String message) {
        if (activity.isFinishing() || activity.isDestroyed()) return;

        String dialogMessage = (message != null && !message.isEmpty())
                ? message
                : activity.getString(R.string.permission_settings_message);

        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.permission_dialog_title)
                .setMessage(dialogMessage)
                .setPositiveButton(R.string.permission_go_to_settings, (dialog, which) -> {
                    openAppSettings(activity);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
