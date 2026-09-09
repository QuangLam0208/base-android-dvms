package com.base.android.ui.main.splash;

import android.text.TextUtils;

import com.base.android.MVVMApplication;
import com.base.android.constant.Constants;
import com.base.android.data.Repository;
import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.data.model.api.request.user.RefreshTokenRequest;
import com.base.android.ui.base.activity.BaseViewModel;
import com.base.android.ui.main.MainCallback;
import com.base.android.utils.JwtUtils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class SplashViewModel extends BaseViewModel {

    public SplashViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    /**
     * Kiểm tra trạng thái xác thực:
     * - Nếu token rỗng hoặc không hợp lệ -> callback trả về false (điều hướng Login).
     * - Nếu token còn hạn -> callback trả về true (điều hướng Main).
     * - Nếu token đã hết hạn hoặc sắp hết hạn (trong vòng 60s):
     *     + Kiểm tra refreshToken: nếu có, gọi API refresh token (giống cơ chế trong AuthInterceptor).
     *     + Nếu refresh thành công -> lưu token mới, callback trả về true (điều hướng Main).
     *     + Nếu refresh thất bại hoặc không có refreshToken -> xóa auth data, callback trả về false (điều hướng Login).
     */
    public void checkAuthentication(MainCallback<Boolean> callback) {
        String token = repository.getToken();

        if (!isValidToken(token)) {
            clearAuthData();
            if (callback != null) {
                callback.doSuccess(false);
            }
            return;
        }

        // Token tồn tại, kiểm tra thời hạn (buffer 60 giây)
        long expiryTime = JwtUtils.getExpiryTime(token);
        boolean isExpiring = (expiryTime <= 0) || JwtUtils.isTokenExpiringSoon(token, 60 * 1000);

        if (!isExpiring) {
            // Token vẫn còn hạn sử dụng
            if (callback != null) {
                callback.doSuccess(true);
            }
            return;
        }

        // Token đã hết hạn hoặc sắp hết hạn -> Thử làm mới token qua refreshToken
        String refreshToken = repository.getSharedPreferences().getRefreshToken();
        if (!isValidToken(refreshToken)) {
            clearAuthData();
            if (callback != null) {
                callback.doSuccess(false);
            }
            return;
        }

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setGrant_type("refresh_token");
        request.setRefresh_token(refreshToken);

        compositeDisposable.add(
                repository.getMasterApiService()
                        .refreshToken(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response != null && !TextUtils.isEmpty(response.getAccessToken())) {
                                        repository.setToken(response.getAccessToken());
                                        if (!TextUtils.isEmpty(response.getRefreshToken())) {
                                            repository.getSharedPreferences().setRefreshToken(response.getRefreshToken());
                                        }
                                        if (callback != null) {
                                            callback.doSuccess(true);
                                        }
                                    } else {
                                        clearAuthData();
                                        if (callback != null) {
                                            callback.doSuccess(false);
                                        }
                                    }
                                },
                                throwable -> {
                                    Timber.e(throwable, "SplashViewModel: Refresh token thất bại");
                                    clearAuthData();
                                    if (callback != null) {
                                        callback.doSuccess(false);
                                    }
                                }
                        )
        );
    }

    private void clearAuthData() {
        this.token = null;
        repository.setToken(Constants.VALUE_BEARER_TOKEN_DEFAULT);
        repository.getSharedPreferences().removeKey(PreferencesService.KEY_BEARER_TOKEN);
        repository.getSharedPreferences().removeKey(PreferencesService.KEY_BEARER_REFRESH_TOKEN);
    }

    private boolean isValidToken(String token) {
        return !TextUtils.isEmpty(token)
                && !Constants.VALUE_BEARER_TOKEN_DEFAULT.equals(token)
                && !"NULL".equalsIgnoreCase(token);
    }
}
