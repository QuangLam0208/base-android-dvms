package com.base.android.ui.main.account.login;

import android.text.TextUtils;

import androidx.databinding.ObservableField;

import com.base.android.MVVMApplication;
import com.base.android.R;
import com.base.android.data.Repository;
import com.base.android.data.model.api.request.user.UserLoginRequest;
import com.base.android.data.model.api.response.user.UserLoginResponse;
import com.base.android.ui.base.activity.BaseViewModel;
import com.base.android.ui.main.MainCallback;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class LoginViewModel extends BaseViewModel {

    public final ObservableField<String> username = new ObservableField<>("");
    public final ObservableField<String> password = new ObservableField<>("");

    public LoginViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public void doLogin(MainCallback<UserLoginResponse> callback) {
        String inputUsername = username.get() != null ? username.get().trim() : "";
        String inputPassword = password.get() != null ? password.get() : "";

        if (TextUtils.isEmpty(inputUsername) || TextUtils.isEmpty(inputPassword)) {
            showWarningMessage(application.getString(R.string.mgs_please_fill_in_all_the_required_information));
            if (callback != null) {
                callback.doFail();
            }
            return;
        }

        UserLoginRequest request = new UserLoginRequest();
        request.setGrantType("password");
        request.setUsername(inputUsername);
        request.setPassword(inputPassword);

        showLoading();
        compositeDisposable.add(
                repository.getMasterApiService()
                        .userLogin(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    hideLoading();
                                    if (response != null && !TextUtils.isEmpty(response.getAccessToken())) {
                                        repository.setToken(response.getAccessToken());
                                        if (!TextUtils.isEmpty(response.getRefreshToken())) {
                                            repository.getSharedPreferences().setRefreshToken(response.getRefreshToken());
                                        }
                                        if (callback != null) {
                                            callback.doSuccess(response);
                                        }
                                    } else {
                                        showErrorMessage("Đăng nhập không thành công, vui lòng kiểm tra lại thông tin");
                                        if (callback != null) {
                                            callback.doFail();
                                        }
                                    }
                                },
                                throwable -> {
                                    Timber.e(throwable);
                                    hideLoading();
                                    showErrorMessage("Đăng nhập thất bại: " + throwable.getMessage());
                                    if (callback != null) {
                                        callback.doError(throwable);
                                    }
                                }
                        )
        );
    }
}
