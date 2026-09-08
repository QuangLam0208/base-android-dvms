package com.base.android.data.model.api.request.user;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class UserLoginRequest {
    @SerializedName("grant_type")
    private String grantType;
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;
}
