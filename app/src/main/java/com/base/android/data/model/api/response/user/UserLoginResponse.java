package com.base.android.data.model.api.response.user;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class UserLoginResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expires_in")
    private Long expiresIn;

    @SerializedName("scope")
    private String scope;

    @SerializedName("user_kind")
    private Integer userKind;

    @SerializedName("user_id")
    private Long userId;

    @SerializedName("grant_type")
    private String grantType;

    @SerializedName("additional_info")
    private String additionalInfo;

    @SerializedName("version")
    private Integer version;

    @SerializedName("jti")
    private String jti;
}