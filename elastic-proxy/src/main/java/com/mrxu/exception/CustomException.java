package com.mrxu.exception;

import com.alibaba.fastjson.JSONObject;
import com.mrxu.model.ErrorMessage;
import io.netty.handler.codec.http.HttpResponseStatus;

public class CustomException extends Exception {
    private String message;
    private int status;

    public CustomException(String type, String reason) {
        this(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), type, reason);
    }

    public CustomException(String message) {
        this("unknown error", message);
    }

    public CustomException(int status, String type, String reason) {
        ErrorMessage msg = new ErrorMessage(status, type, reason);
        this.message = JSONObject.toJSONString(msg);
        this.status = status;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public int getStatus() {
        return status;
    }
}
