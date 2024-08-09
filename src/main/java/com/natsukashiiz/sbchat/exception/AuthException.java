package com.natsukashiiz.sbchat.exception;

public class AuthException extends BaseException {

    public AuthException(String code) {
        super("auth." + code);
    }

    public static AuthException unauthorized() {
        return new AuthException("unauthorized");
    }
}