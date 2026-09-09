package com.base.android.data.model.api.response.company;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class CompanyResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("avatar")
    private String avatar;
}
