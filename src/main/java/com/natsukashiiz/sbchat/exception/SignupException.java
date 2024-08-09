package com.natsukashiiz.sbchat.exception;

public class SignupException extends BaseException {
    public SignupException(String code) {
        super("signup." + code);
    }

    public static SignupException invalid() {
        return new SignupException("invalid");
    }

    public static SignupException usernameInvalid() {
        return new SignupException("username.invalid");
    }

    public static SignupException mobileInvalid() {
        return new SignupException("mobile.invalid");
    }

    public static SignupException usernameExists() {
        return new SignupException("username.exists");
    }

    public static SignupException mobileExists() {
        return new SignupException("mobile.exists");
    }
}
