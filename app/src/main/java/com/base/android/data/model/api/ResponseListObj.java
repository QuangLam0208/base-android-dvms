package com.base.android.data.model.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;

@Data
public class ResponseListObj<T> {
    @SerializedName("content")
    private List<T> content;

    @SerializedName("page")
    private Integer page;

    @SerializedName("totalPages")
    private Integer totalPages;

    @SerializedName("totalElements")
    private Long totalElements;
}
