package com.base.android.data.model.api.response.course;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;

@Data
public class CourseResponse {
    @SerializedName("id")
    private Long id;

    @SerializedName("status")
    private Integer status;

    @SerializedName("modifiedDate")
    private String modifiedDate;

    @SerializedName("createdDate")
    private String createdDate;

    @SerializedName("name")
    private String name;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("price")
    private Double price;

    @SerializedName("shortDescription")
    private String shortDescription;

    @SerializedName("totalTimeline")
    private Integer totalTimeline;

    @SerializedName("syllabuses")
    private List<SyllabusResponse> syllabuses;
}

