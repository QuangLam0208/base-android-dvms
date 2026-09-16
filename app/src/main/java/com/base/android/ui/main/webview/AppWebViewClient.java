package com.base.android.ui.main.webview;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.base.android.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class AppWebViewClient extends WebViewClient {

    public interface ClientCallback {
        void onPageStarted(String url);
        void onPageFinished(String url);
        void onErrorReceived(String errorDescription);
    }

    private final WeakReference<Activity> activityRef;
    private final ClientCallback callback;

    public AppWebViewClient(Activity activity, ClientCallback callback) {
        this.activityRef = new WeakReference<>(activity);
        this.callback = callback;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return handleUrlOverride(view, url);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return handleUrlOverride(view, request.getUrl().toString());
    }

    private boolean handleUrlOverride(WebView view, String url) {
        if (url == null) return false;

        // Nếu là HTTP hoặc HTTPS thông thường, để WebView tự tải
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("file:///")) {
            return false;
        }

        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) return true;

        try {
            // Xử lý các scheme intent:// (thường dùng cho App Links, Ngân hàng, Play Store)
            if (url.startsWith("intent://")) {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                if (intent != null) {
                    try {
                        activity.startActivity(intent);
                        return true;
                    } catch (ActivityNotFoundException e) {
                        // Thử mở fallback URL nếu ứng dụng chưa cài đặt
                        String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                        if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
                            view.loadUrl(fallbackUrl);
                            return true;
                        }
                        // Hoặc mở Play Store nếu có package name
                        String packageName = intent.getPackage();
                        if (packageName != null && !packageName.isEmpty()) {
                            activity.startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + packageName)));
                            return true;
                        }
                    }
                }
            } else {
                // Xử lý các scheme tel:, mailto:, sms:, market:, vnpay:, zalopay:, fb:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                activity.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            Timber.e(e, "Cannot handle URL scheme: %s", url);
            Toast.makeText(activity, "Không tìm thấy ứng dụng phù hợp để mở liên kết này", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (callback != null) {
            callback.onPageStarted(url);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (callback != null) {
            callback.onPageFinished(url);
        }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        // Chỉ xử lý lỗi khi xảy ra ở main frame (không phải các file phụ như css, img)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request.isForMainFrame()) {
            if (callback != null) {
                callback.onErrorReceived(error.getDescription() != null ? error.getDescription().toString() : "Lỗi kết nối");
            }
        }
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (callback != null) {
                callback.onErrorReceived(description);
            }
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) {
            handler.cancel();
            return;
        }

        // Cảnh báo người dùng an toàn thay vì tự động bypass SSL
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.webview_ssl_error_title)
                .setMessage(R.string.webview_ssl_error_message)
                .setPositiveButton(R.string.webview_ssl_continue, (dialog, which) -> handler.proceed())
                .setNegativeButton(R.string.webview_ssl_cancel, (dialog, which) -> handler.cancel())
                .setCancelable(false)
                .show();
    }
}
