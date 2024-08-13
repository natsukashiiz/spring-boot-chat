package com.natsukashiiz.sbchat.exception;

public class ProfileException extends BaseException {

    public ProfileException(String code) {
        super("auth." + code);
    }

    public static BaseException usernameExists() {
        return new ProfileException("username.exists");
    }

    public static BaseException mobileExists() {
        return new ProfileException("mobile.exists");
    }

    public static BaseException passwordNotMatch() {
        return new ProfileException("password.not.match");
    }
}