package com.natsukashiiz.sbchat.exception;

public class LoginException extends BaseException {
    public LoginException(String code) {
        super("login." + code);
    }

    public static LoginException invalid() {
        return new LoginException("invalid");
    }

    public static LoginException identifierInvalid() {
        return new LoginException("identifier.invalid");
    }
}
