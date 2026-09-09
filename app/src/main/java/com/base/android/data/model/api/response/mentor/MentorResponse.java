package com.base.android.data.model.api.response.mentor;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class MentorResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("position")
    private String position;

    @SerializedName("description")
    private String description;

    @SerializedName("account")
    private MentorAccountResponse account;
}
