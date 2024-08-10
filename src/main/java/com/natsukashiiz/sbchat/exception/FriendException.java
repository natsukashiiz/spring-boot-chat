package com.natsukashiiz.sbchat.exception;

public class FriendException extends BaseException {

    public FriendException(String code) {
        super("friend." + code);
    }

    public static BaseException applyToSelf() {
        return new FriendException("apply.to.self");
    }

    public static BaseException notFound() {
        return new FriendException("not.found");
    }

    public static BaseException duplicate() {
        return new FriendException("duplicate");
    }

    public static BaseException notApply() {
        return new FriendException("not.apply");
    }

    public static BaseException notFriend() {
        return new FriendException("not.friend");
    }

    public static BaseException notBlocked() {
        return new FriendException("not.blocked");
    }
}