package com.mrxu.exception;

import com.alibaba.fastjson.JSONObject;
import com.mrxu.model.ErrorMsg;

public class CustomException extends Exception {
    private String message;

    public CustomException(String type, String reason) {
        ErrorMsg msg = new ErrorMsg(type, reason);
        this.message = JSONObject.toJSONString(msg);
    }
    public CustomException(String message) {
        this.message = message;
    }

    public CustomException(int status, String type, String reason) {
        ErrorMsg msg = new ErrorMsg(status, type, reason);
        this.message = JSONObject.toJSONString(msg);
    }


    @Override
    public String getMessage() {
        return this.message;
    }
}
