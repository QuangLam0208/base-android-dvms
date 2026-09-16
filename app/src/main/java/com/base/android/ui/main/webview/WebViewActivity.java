package com.base.android.ui.main.webview;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.base.android.BR;
import com.base.android.R;
import com.base.android.databinding.ActivityWebviewBinding;
import com.base.android.di.component.ActivityComponent;
import com.base.android.ui.base.activity.BaseActivity;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class WebViewActivity extends BaseActivity<ActivityWebviewBinding, WebViewViewModel> {

    public static final String EXTRA_URL = "EXTRA_URL";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_SHOW_TOOLBAR = "EXTRA_SHOW_TOOLBAR";
    public static final String EXTRA_SYNC_TOKEN = "EXTRA_SYNC_TOKEN";

    private static final int REQUEST_FILE_CHOOSER = 2001;

    private ValueCallback<Uri[]> filePathCallback;
    private String targetUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding.setA(this);
        viewBinding.setVm(viewModel);

        readIntentData();
        setupCookieManager();
        setupWebSettings();
        setupClients();
        setupBridge();
        loadTargetUrl();
    }

    private void readIntentData() {
        Intent intent = getIntent();
        targetUrl = intent.getStringExtra(EXTRA_URL);
        String initialTitle = intent.getStringExtra(EXTRA_TITLE);
        boolean showToolbar = intent.getBooleanExtra(EXTRA_SHOW_TOOLBAR, true);

        viewModel.setInitialTitle(initialTitle);
        viewModel.setShowToolbar(showToolbar);
        viewModel.updateUrl(targetUrl);
    }

    private void setupCookieManager() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(viewBinding.webView, true);
        }
    }

    private void setupWebSettings() {
        WebSettings settings = viewBinding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
    }

    private void setupClients() {
        // WebChromeClient: Theo dõi tiến trình, tiêu đề web, upload file
        viewBinding.webView.setWebChromeClient(new AppWebChromeClient(this, new AppWebChromeClient.ChromeCallback() {
            @Override
            public void onProgressChanged(int newProgress) {
                viewModel.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(String title) {
                viewModel.updatePageTitle(title);
            }

            @Override
            public boolean onShowFileChooser(ValueCallback<Uri[]> callback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = callback;

                Intent intent = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && fileChooserParams != null) {
                    try {
                        intent = fileChooserParams.createIntent();
                    } catch (Exception e) {
                        Timber.w(e, "createIntent from FileChooserParams failed, falling back to GET_CONTENT");
                    }
                }
                if (intent == null) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                }

                try {
                    startActivityForResult(Intent.createChooser(intent, "Chọn tệp tin"), REQUEST_FILE_CHOOSER);
                    return true;
                } catch (Exception e) {
                    Timber.e(e, "Error launching file chooser");
                    if (filePathCallback != null) {
                        filePathCallback.onReceiveValue(null);
                        filePathCallback = null;
                    }
                    return false;
                }
            }
        }));

        // WebViewClient: Xử lý URL redirects, lỗi tải trang, cảnh báo SSL
        viewBinding.webView.setWebViewClient(new AppWebViewClient(this, new AppWebViewClient.ClientCallback() {
            @Override
            public void onPageStarted(String url) {
                viewModel.setError(false, null);
                viewModel.updateUrl(url);
            }

            @Override
            public void onPageFinished(String url) {
                viewModel.updateUrl(url);
            }

            @Override
            public void onErrorReceived(String errorDescription) {
                viewModel.setError(true, errorDescription);
            }
        }));
    }

    private void setupBridge() {
        viewBinding.webView.addJavascriptInterface(new WebAppInterface(this, new WebAppInterface.BridgeCallback() {
            @Override
            public void onCloseRequested() {
                finish();
            }

            @Override
            public String onTokenRequested() {
                return viewModel.getTokenFromRepository();
            }
        }), "AppBridge");
    }

    private void loadTargetUrl() {
        if (targetUrl == null || targetUrl.trim().isEmpty()) {
            viewModel.setError(true, "Không có địa chỉ URL được cung cấp.");
            return;
        }

        boolean syncToken = getIntent().getBooleanExtra(EXTRA_SYNC_TOKEN, false);
        if (syncToken) {
            String authToken = viewModel.getTokenFromRepository();
            if (authToken != null && !authToken.isEmpty()) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + authToken);
                viewBinding.webView.loadUrl(targetUrl, headers);
                return;
            }
        }

        viewBinding.webView.loadUrl(targetUrl);
    }

    // Các hàm tương tác được gọi từ XML DataBinding
    public void onBackClick() {
        if (viewBinding.webView.canGoBack()) {
            viewBinding.webView.goBack();
        } else {
            finish();
        }
    }

    public void onCloseClick() {
        finish();
    }

    public void onReloadClick() {
        viewModel.setError(false, null);
        viewBinding.webView.reload();
    }

    public void onOpenInBrowserClick() {
        String currentUrl = viewBinding.webView.getUrl();
        if (currentUrl == null || currentUrl.isEmpty()) {
            currentUrl = targetUrl;
        }
        if (currentUrl != null && !currentUrl.isEmpty()) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Không thể mở trình duyệt ngoài", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onRetryClick() {
        viewModel.setError(false, null);
        loadTargetUrl();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILE_CHOOSER) {
            if (filePathCallback != null) {
                Uri[] results = null;
                if (resultCode == RESULT_OK && data != null) {
                    if (data.getData() != null) {
                        results = new Uri[]{data.getData()};
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        results = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                    }
                }
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (viewBinding.webView.canGoBack()) {
            viewBinding.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewBinding.webView != null) {
            viewBinding.webView.onResume();
            viewBinding.webView.resumeTimers();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewBinding.webView != null) {
            viewBinding.webView.onPause();
            viewBinding.webView.pauseTimers();
        }
    }

    @Override
    protected void onDestroy() {
        if (viewBinding != null && viewBinding.webView != null) {
            WebView webView = viewBinding.webView;
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
            if (webView.getParent() instanceof ViewGroup) {
                ((ViewGroup) webView.getParent()).removeView(webView);
            }
            webView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_webview;
    }

    @Override
    public int getBindingVariable() {
        return BR.vm;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }
}
