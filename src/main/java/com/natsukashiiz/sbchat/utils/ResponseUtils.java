package com.natsukashiiz.sbchat.utils;

import com.natsukashiiz.sbchat.model.response.SuccessResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtils {

    private final static HttpStatus SUCCESS = HttpStatus.OK;

    public static ResponseEntity<Object> success() {
        var response = new SuccessResponse();
        response.setStatus(SUCCESS.value());
        response.setData(null);
        return new ResponseEntity<>(response, SUCCESS);
    }

    public static ResponseEntity<Object> success(Object data) {
        var response = new SuccessResponse();
        response.setStatus(SUCCESS.value());
        response.setData(data);
        return new ResponseEntity<>(response, SUCCESS);
    }

    public static ResponseEntity<Object> successList(Object data, long total) {
        var response = new SuccessResponse();
        response.setStatus(SUCCESS.value());
        response.setData(data);
        response.setTotal(total);
        return new ResponseEntity<>(response, SUCCESS);
    }
}
