package com.base.android.data.model.api.response.mentor;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class MentorAccountResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("kind")
    private Integer kind;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("avatarPath")
    private String avatarPath;
}
