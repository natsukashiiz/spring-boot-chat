package com.natsukashiiz.sbchat.exception;

public class RoomException extends BaseException {

    public RoomException(String code) {
        super("room." + code);
    }

    public static BaseException notFound() {
        return new RoomException("not.found");
    }
}