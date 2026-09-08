package com.base.android.data.model.api.response.classroom;

import com.base.android.data.model.api.response.course.CourseResponse;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class ClassRoomResponse {
    @SerializedName("id")
    private Long id;

    @SerializedName("status")
    private Integer status;

    @SerializedName("modifiedDate")
    private String modifiedDate;

    @SerializedName("createdDate")
    private String createdDate;

    @SerializedName("course")
    private CourseResponse course;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("state")
    private Integer state;

    @SerializedName("price")
    private Double price;
}
