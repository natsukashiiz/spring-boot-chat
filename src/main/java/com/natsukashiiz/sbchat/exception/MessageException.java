package com.natsukashiiz.sbchat.exception;

public class MessageException extends BaseException {

    public MessageException(String code) {
        super("message." + code);
    }

    public static BaseException notMember() {
        return new MessageException("not.member");
    }
}