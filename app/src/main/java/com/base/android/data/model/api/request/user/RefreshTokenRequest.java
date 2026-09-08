package com.base.android.data.model.api.request.user;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refresh_token;
    private String grant_type;
}
