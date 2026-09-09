package com.base.android.data.model.api.response.rating;

import com.base.android.data.model.api.response.course.CourseResponse;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class RatingResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("course")
    private CourseResponse course;

    @SerializedName("student")
    private RatingStudent student;

    @SerializedName("message")
    private String message;

    @SerializedName("star")
    private Integer star;

    @Data
    public static class RatingStudent {
        @SerializedName("id")
        private Long id;

        @SerializedName("status")
        private Integer status;

        @SerializedName("createdDate")
        private String createdDate;

        @SerializedName("account")
        private RatingAccount account;
    }

    @Data
    public static class RatingAccount {
        @SerializedName("id")
        private Long id;

        @SerializedName("kind")
        private Integer kind;

        @SerializedName("username")
        private String username;

        @SerializedName("fullName")
        private String fullName;

        @SerializedName("avatarPath")
        private String avatarPath;
    }
}