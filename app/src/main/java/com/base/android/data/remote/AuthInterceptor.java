package com.base.android.data.remote;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.base.android.BuildConfig;
import com.base.android.constant.Constants;
import com.base.android.data.local.prefs.PreferencesService;
import com.base.android.data.model.api.request.user.RefreshTokenRequest;
import com.base.android.data.model.api.response.login.LoginResponse;
import com.base.android.data.model.api.response.user.UserLoginResponse;
import com.base.android.utils.JwtUtils;
import com.base.android.utils.LogService;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthInterceptor implements Interceptor {

    private final PreferencesService appPreferences;
    private final Application application;
    private final MasterApiService refreshApiService;

    public AuthInterceptor(PreferencesService appPreferences, Application application) {
        this.appPreferences = appPreferences;
        this.application = application;

        // OkHttpClient riêng biệt chuyên phục vụ cho việc refresh token
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();
                    String credentials = "abc_client:abc123";
                    String basicAuth = "Basic " + android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);
                    builder.header("Authorization", basicAuth);
                    builder.header("X-tenant", "moviehub");
                    return chain.proceed(builder.build());
                })
                .build();

        Retrofit refreshRetrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.MASTER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        this.refreshApiService = refreshRetrofit.create(MasterApiService.class);
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        String ignoreAuth = originalRequest.header("IgnoreAuth");
        String useBasicAuth = originalRequest.header("UseBasicAuth");

        requestBuilder.removeHeader("IgnoreAuth");
        requestBuilder.removeHeader("UseBasicAuth");
        requestBuilder.header("X-tenant", "moviehub");

        // TRƯỜNG HỢP 1: Bỏ qua xác thực
        if ("1".equals(ignoreAuth)) {
            return chain.proceed(requestBuilder.build());
        }

        // TRƯỜNG HỢP 2: Sử dụng Basic Auth
        if ("1".equals(useBasicAuth)) {
            String credentials = "abc_client:abc123";
            String basicAuth = "Basic " + android.util.Base64.encodeToString(credentials.getBytes(), android.util.Base64.NO_WRAP);
            requestBuilder.header("Authorization", basicAuth);
            return chain.proceed(requestBuilder.build());
        }

        // TRƯỜNG HỢP 3: Sử dụng Bearer Token của User
        String token = appPreferences.getToken();
        if (isValidToken(token)) {
            if (JwtUtils.isTokenExpiringSoon(token, 2 * 60 * 1000)) {
                synchronized (this) {
                    String freshToken = refreshTokenSync();
                    if (freshToken != null) {
                        token = freshToken;
                    }
                }
            }
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        Response response = chain.proceed(requestBuilder.build());

        if (response.code() == 401) {
            synchronized (this) {
                String latestToken = appPreferences.getToken();
                String newToken;

                // Kiểm tra xem đã có luồng khác vừa refresh token xong hay chưa
                if (isValidToken(latestToken) && !latestToken.equals(token)) {
                    newToken = latestToken;
                } else {
                    newToken = refreshTokenSync();
                }

                if (newToken != null) {
                    response.close(); // Đóng kết nối response cũ trước khi retry

                    Request retryRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + newToken)
                            .header("X-tenant", "moviehub")
                            .build();

                    LogService.i("Retrying request with new token...");
                    return chain.proceed(retryRequest);
                } else {
                    LogService.e("Refresh Token failed. Session expired.");
                    handleLogout();
                }
            }
        }

        return response;
    }

    private synchronized String refreshTokenSync() {
        String refreshToken = appPreferences.getRefreshToken();
        if (!isValidToken(refreshToken)) {
            return null;
        }

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setGrant_type("refresh_token");
        request.setRefresh_token(refreshToken);

        try {
            retrofit2.Response<UserLoginResponse> res = refreshApiService.refreshTokenSync(request).execute();

            if (res.isSuccessful() && res.body() != null) {
                String newAccessToken = res.body().getAccessToken();
                String newRefreshToken = res.body().getRefreshToken();

                appPreferences.setToken(newAccessToken);
                if (!TextUtils.isEmpty(newRefreshToken)) {
                    appPreferences.setRefreshToken(newRefreshToken);
                }

                LogService.i("Refresh token successful!");
                return newAccessToken;
            }
        } catch (Exception e) {
            LogService.e("Error during refresh token: " + e.getMessage());
        }
        return null;
    }

    private void handleLogout() {
        appPreferences.removeKey(PreferencesService.KEY_BEARER_TOKEN);
        appPreferences.removeKey(PreferencesService.KEY_BEARER_REFRESH_TOKEN);

        Intent intent = new Intent(Constants.ACTION_EXPIRED_TOKEN);
        LocalBroadcastManager.getInstance(application.getApplicationContext()).sendBroadcast(intent);
    }

    private boolean isValidToken(String token) {
        return !TextUtils.isEmpty(token)
                && !Constants.VALUE_BEARER_TOKEN_DEFAULT.equals(token)
                && !"NULL".equalsIgnoreCase(token);
    }
}
