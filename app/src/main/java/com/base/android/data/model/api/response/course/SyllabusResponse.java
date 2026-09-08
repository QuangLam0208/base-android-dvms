package com.base.android.data.model.api.response.course;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class SyllabusResponse {
    @SerializedName("id")
    private Long id;

    @SerializedName("status")
    private Integer status;

    @SerializedName("modifiedDate")
    private String modifiedDate;

    @SerializedName("createdDate")
    private String createdDate;

    @SerializedName("course")
    private String course;

    @SerializedName("kind")
    private Integer kind;

    @SerializedName("name")
    private String name;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("description")
    private String description;

    @SerializedName("ordering")
    private Integer ordering;

    @SerializedName("timeline")
    private Integer timeline;
}
