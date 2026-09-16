package com.base.android.ui.main.webview;

import android.net.Uri;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.base.android.MVVMApplication;
import com.base.android.data.Repository;
import com.base.android.ui.base.activity.BaseViewModel;

public class WebViewViewModel extends BaseViewModel {

    public final ObservableField<String> title = new ObservableField<>("");
    public final ObservableField<String> subtitle = new ObservableField<>("");
    public final ObservableField<String> currentUrl = new ObservableField<>("");
    public final ObservableInt progress = new ObservableInt(0);
    public final ObservableBoolean showToolbar = new ObservableBoolean(true);
    public final ObservableBoolean hasError = new ObservableBoolean(false);
    public final ObservableField<String> errorMessage = new ObservableField<>("");

    private boolean staticTitleSet = false;

    public WebViewViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void setInitialTitle(String initialTitle) {
        if (initialTitle != null && !initialTitle.trim().isEmpty()) {
            title.set(initialTitle);
            staticTitleSet = true;
        }
    }

    public void updatePageTitle(String pageTitle) {
        // Chỉ cập nhật title từ web nếu không có static title truyền từ bên ngoài
        if (!staticTitleSet && pageTitle != null && !pageTitle.trim().isEmpty()) {
            title.set(pageTitle);
        }
    }

    public void updateUrl(String url) {
        currentUrl.set(url);
        if (url != null) {
            try {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                if (host != null && !host.isEmpty()) {
                    subtitle.set(host);
                } else {
                    subtitle.set(url);
                }
            } catch (Exception e) {
                subtitle.set(url);
            }
        }
    }

    public void setProgress(int newProgress) {
        progress.set(newProgress);
    }

    public void setShowToolbar(boolean isVisible) {
        showToolbar.set(isVisible);
    }

    public void setError(boolean isError, String message) {
        hasError.set(isError);
        errorMessage.set(message);
    }

    public String getTokenFromRepository() {
        if (token != null && !token.isEmpty()) {
            return token;
        }
        return repository != null ? repository.getToken() : "";
    }
}
