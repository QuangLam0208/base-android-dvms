package com.base.android.data.remote;

import io.reactivex.rxjava3.core.Observable;

import com.base.android.data.model.api.ResponseListObj;
import com.base.android.data.model.api.ResponseWrapper;
import com.base.android.data.model.api.request.login.LoginRequest;
import com.base.android.data.model.api.response.classroom.ClassRoomResponse;
import com.base.android.data.model.api.response.company.CompanyResponse;
import com.base.android.data.model.api.response.login.LoginResponse;
import com.base.android.data.model.api.response.mentor.MentorResponse;
import com.base.android.data.model.api.response.rating.RatingResponse;

import java.util.Map;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface ApiService {
    @POST("/v1/employee/login")
    @Headers({"IgnoreAuth: 1"})
    Observable<ResponseWrapper<LoginResponse>> login(@Body LoginRequest request);

    @GET("/v1/class-room/public/list")
    @Headers({"IgnoreAuth: 1"})
    Observable<ResponseWrapper<ResponseListObj<ClassRoomResponse>>> getListClassRoom(@QueryMap Map<String, Object> query);

    @GET("/v1/mentor/public/list")
    @Headers({"IgnoreAuth: 1"})
    Observable<ResponseWrapper<ResponseListObj<MentorResponse>>> getPublicMentors();

    @GET("/v1/rating/public/list")
    @Headers({"IgnoreAuth: 1"})
    Observable<ResponseWrapper<ResponseListObj<RatingResponse>>> getPublicRatings();

    @GET("/v1/company/public/list")
    @Headers({"IgnoreAuth: 1"})
    Observable<ResponseWrapper<ResponseListObj<CompanyResponse>>> getPublicCompanies();
}
