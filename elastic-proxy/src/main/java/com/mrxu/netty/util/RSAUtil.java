package com.mrxu.netty.util;

import java.util.Base64;

public class RSAUtil {

    public static String encode(String password) {
        byte[] passwords = Base64.getEncoder().encode(password.getBytes());
        return new String(passwords);
    }

    public static String decode(String password) {
        //历史的密码都是明文的
        byte[] passwords = Base64.getDecoder().decode(password);
        return new String(passwords);
    }
}
