package com.base.android.data.remote;

import com.base.android.data.model.api.request.user.RefreshTokenRequest;
import com.base.android.data.model.api.request.user.UserLoginRequest;
import com.base.android.data.model.api.response.login.LoginResponse;
import com.base.android.data.model.api.response.user.UserLoginResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface MasterApiService {
    @POST("api/token")
    @Headers({"UseBasicAuth: 1"})
    Observable<UserLoginResponse> userLogin(@Body UserLoginRequest request);

    @POST("api/token")
    @Headers({"UseBasicAuth: 1"})
    Observable<UserLoginResponse> refreshToken(@Body RefreshTokenRequest request);

    @POST("api/token")
    @Headers({"UseBasicAuth: 1"})
    Call<UserLoginResponse> refreshTokenSync(@Body RefreshTokenRequest request);
}
