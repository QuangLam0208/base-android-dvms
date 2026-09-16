package com.base.android.ui.main.webview;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.base.android.BuildConfig;
import com.google.gson.JsonObject;

import java.lang.ref.WeakReference;

public class WebAppInterface {

    public interface BridgeCallback {
        void onCloseRequested();
        String onTokenRequested();
    }

    private final WeakReference<Activity> activityRef;
    private final BridgeCallback callback;
    private final Handler mainHandler;

    public WebAppInterface(Activity activity, BridgeCallback callback) {
        this.activityRef = new WeakReference<>(activity);
        this.callback = callback;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @JavascriptInterface
    public void showToast(String message) {
        if (message == null) return;
        mainHandler.post(() -> {
            Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @JavascriptInterface
    public void closeWindow() {
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onCloseRequested();
            } else {
                Activity activity = activityRef.get();
                if (activity != null && !activity.isFinishing()) {
                    activity.finish();
                }
            }
        });
    }

    @JavascriptInterface
    public String getToken() {
        if (callback != null) {
            String token = callback.onTokenRequested();
            return token != null ? token : "";
        }
        return "";
    }

    @JavascriptInterface
    public String getDeviceInfo() {
        JsonObject json = new JsonObject();
        json.addProperty("appVersion", BuildConfig.VERSION_NAME);
        json.addProperty("appVersionCode", BuildConfig.VERSION_CODE);
        json.addProperty("deviceModel", Build.MODEL);
        json.addProperty("deviceManufacturer", Build.MANUFACTURER);
        json.addProperty("osVersion", Build.VERSION.RELEASE);
        json.addProperty("sdkInt", Build.VERSION.SDK_INT);
        return json.toString();
    }

    @JavascriptInterface
    public void postMessage(String action, String data) {
        if ("CLOSE".equalsIgnoreCase(action)) {
            closeWindow();
        } else if ("TOAST".equalsIgnoreCase(action)) {
            showToast(data);
        }
    }
}
