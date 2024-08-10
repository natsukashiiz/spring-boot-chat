package com.natsukashiiz.sbchat.utils;

import com.natsukashiiz.sbchat.model.response.ApiResponse;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ResponseUtils {

    private final static HttpStatus SUCCESS = HttpStatus.OK;

    public static ApiResponse<Object> success() {
        var response = new ApiResponse<>();
        response.setStatus(SUCCESS.value());
        response.setData(null);
        return response;
    }

    public static <T> ApiResponse<T> success(T data) {
        var response = new ApiResponse<T>();
        response.setStatus(SUCCESS.value());
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<List<T>> successList(List<T> data, long total) {
        var response = new ApiResponse<List<T>>();
        response.setStatus(SUCCESS.value());
        response.setData(data);
        response.setTotal(total);
        return response;
    }

    public static <T> ApiResponse<List<T>> successList(List<T> data) {
        return successList(data, data.size());
    }
}
