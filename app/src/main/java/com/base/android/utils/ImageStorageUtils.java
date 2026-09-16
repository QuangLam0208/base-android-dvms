package com.base.android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public final class ImageStorageUtils {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface SaveCallback {
        void onSuccess(File savedFile);
        default void onError(Throwable throwable) {}
    }

    private ImageStorageUtils() {
        // Utility class
    }

    /**
     * Nén ảnh qua Glide và ghi vào file đích trên Background Thread.
     * Áp dụng Atomic write qua file tạm để tránh tạo file rỗng (0 bytes).
     *
     * @param context   Application context hoặc Context hiện tại
     * @param sourceUri Uri của ảnh nguồn (từ camera, gallery hoặc uCrop)
     * @param destFile  File đích cần lưu vào (ví dụ user_avatar.jpg)
     * @param maxSize   Kích thước cạnh tối đa (ví dụ 512)
     * @param callback  Callback trả về kết quả trên Main Thread
     */
    public static void saveImageAsync(@NonNull Context context,
                                      @NonNull Uri sourceUri,
                                      @NonNull File destFile,
                                      int maxSize,
                                      @Nullable SaveCallback callback) {
        Context appContext = context.getApplicationContext();

        executor.execute(() -> {
            FutureTarget<Bitmap> futureTarget = null;
            // Tạo file tạm để lưu ảnh sau khi crop
            File tempFile = new File(appContext.getFilesDir(), "temp_" + System.currentTimeMillis() + ".jpg");

            try {
                // Sử dụng Glide để tự động giải mã HEIC/HEIF, xoay chuẩn EXIF và resize
                futureTarget = Glide.with(appContext)
                        .asBitmap()
                        .load(sourceUri)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .signature(new ObjectKey(System.currentTimeMillis()))
                        .centerCrop()
                        .submit(maxSize, maxSize);

                Bitmap bitmap = futureTarget.get(); // chờ đợi kết quả và lấy Bitmap

                if (bitmap != null && !bitmap.isRecycled()) {
                    FileOutputStream os = new FileOutputStream(tempFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, os); // compress ảnh
                    os.flush();
                    os.close();

                    if (tempFile.exists() && tempFile.length() > 0) {
                        if (destFile.exists()) {
                            destFile.delete();
                        }
                        tempFile.renameTo(destFile); // lưu vào file đích

                        Timber.d("Lưu ảnh thành công vào: %s (%d KB)", destFile.getAbsolutePath(), destFile.length() / 1024);

                        if (callback != null) {
                            mainHandler.post(() -> callback.onSuccess(destFile)); // thông báo kết quả trên Main Thread
                        }
                    } else {
                        throw new IllegalStateException("Lỗi ghi file tạm, file rỗng.");
                    }
                } else {
                    throw new IllegalStateException("Không thể tạo Bitmap từ Uri: " + sourceUri);
                }
            } catch (Exception e) {
                Timber.e(e, "Lỗi khi nén và lưu ảnh bằng ImageStorageUtils");
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(e));
                }
            } finally {
                if (futureTarget != null) {
                    Glide.with(appContext).clear(futureTarget);
                }
                // Dọn dẹp file tạm thời trong cacheDir (ví dụ file crop của uCrop)
                if ("file".equals(sourceUri.getScheme()) && sourceUri.getPath() != null) {
                    File sourceFile = new File(sourceUri.getPath());
                    if (sourceFile.exists() && sourceFile.getAbsolutePath().contains(appContext.getCacheDir().getAbsolutePath())) {
                        sourceFile.delete();
                    }
                }
            }
        });
    }
}
