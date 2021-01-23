package com.mrxu.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 将异常包装秤类似于es返回异常数据的格式
 */
@Data
public class ErrorMsg {
//    返回状态码
    private int status;
//    错误类型
    private String type;
//    错误原因
    private String reason;
//    详细信息
    private Error error;

    @Data
    private static class Error {
        private Msg[] root_case;
    }

    @Data
    @AllArgsConstructor
    private static class Msg {
        private String type;
        private String reason;
    }

    public ErrorMsg(String type, String reason) {
        Msg msg = new Msg(type, reason);
        Msg[] msgs = new Msg[]{msg};
        Error error = new Error();
        error.setRoot_case(msgs);
        this.type = type;
        this.reason = reason;
        this.error = error;
    }

    public ErrorMsg(int status, String type, String reason) {
        this(type, reason);
        this.status = status;
    }
}