package com.base.android.utils;

import android.content.Context;
import android.content.Intent;

import com.base.android.ui.main.webview.WebViewActivity;

public class WebViewUtils {

    public static final String URL_TUOITRE = "https://tuoitre.vn/";
    public static final String URL_FACEBOOK = "https://www.facebook.com/";

    /**
     * Mở một URL bất kỳ trong WebView với các cấu hình mặc định
     */
    public static void openUrl(Context context, String url) {
        openUrl(context, url, null, true, false);
    }

    /**
     * Mở một URL với tiêu đề định sẵn
     */
    public static void openUrl(Context context, String url, String title) {
        openUrl(context, url, title, true, false);
    }

    /**
     * Mở một URL với đầy đủ các tùy chọn
     */
    public static void openUrl(Context context, String url, String title, boolean showToolbar, boolean syncToken) {
        if (context == null || url == null || url.trim().isEmpty()) {
            return;
        }

        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url.trim());
        if (title != null && !title.trim().isEmpty()) {
            intent.putExtra(WebViewActivity.EXTRA_TITLE, title.trim());
        }
        intent.putExtra(WebViewActivity.EXTRA_SHOW_TOOLBAR, showToolbar);
        intent.putExtra(WebViewActivity.EXTRA_SYNC_TOKEN, syncToken);

        context.startActivity(intent);
    }

    /**
     * Mở trực tiếp Báo Tuổi Trẻ Online
     */
    public static void openTuoitre(Context context) {
        openUrl(context, URL_TUOITRE, "Báo Tuổi Trẻ Online");
    }

    /**
     * Mở trực tiếp Facebook Web
     */
    public static void openFacebook(Context context) {
        openUrl(context, URL_FACEBOOK, "Facebook");
    }
}
