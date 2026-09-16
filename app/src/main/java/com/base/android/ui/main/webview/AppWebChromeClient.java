package com.base.android.ui.main.webview;

import android.app.Activity;
import android.net.Uri;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;

public class AppWebChromeClient extends WebChromeClient {

    public interface ChromeCallback {
        void onProgressChanged(int newProgress);
        void onReceivedTitle(String title);
        boolean onShowFileChooser(ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams);
    }

    private final WeakReference<Activity> activityRef;
    private final ChromeCallback callback;

    public AppWebChromeClient(Activity activity, ChromeCallback callback) {
        this.activityRef = new WeakReference<>(activity);
        this.callback = callback;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (callback != null) {
            callback.onProgressChanged(newProgress);
        }
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (callback != null) {
            callback.onReceivedTitle(title);
        }
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        if (callback != null) {
            return callback.onShowFileChooser(filePathCallback, fileChooserParams);
        }
        return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) {
            result.cancel();
            return true;
        }

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Thông báo")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> result.confirm())
                .setOnCancelListener(dialog -> result.cancel())
                .setCancelable(false)
                .show();

        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        Activity activity = activityRef.get();
        if (activity == null || activity.isFinishing()) {
            result.cancel();
            return true;
        }

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Xác nhận")
                .setMessage(message)
                .setPositiveButton("Đồng ý", (dialog, which) -> result.confirm())
                .setNegativeButton("Hủy", (dialog, which) -> result.cancel())
                .setOnCancelListener(dialog -> result.cancel())
                .setCancelable(false)
                .show();

        return true;
    }
}
