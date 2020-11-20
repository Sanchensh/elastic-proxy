package com.mrxu.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ErrorMsg {
    private int status = 400;
    private String type;
    private String reason;
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
    public ErrorMsg(int status,String type, String reason) {
        this(type,reason);
        this.status = status;
    }
}