package com.natsukashiiz.sbchat.exception;

public class GroupException extends BaseException {

    public GroupException(String code) {
        super("group." + code);
    }

    public static BaseException nameInvalid() {
        return new GroupException("name.invalid");
    }

    public static BaseException memberInvalid() {
        return new GroupException("member.invalid");
    }

    public static BaseException notFound() {
        return new GroupException("not.found");
    }

    public static BaseException notOwner() {
        return new GroupException("not.owner");
    }

    public static BaseException notPermission() {
        return new GroupException("not.permission");
    }
}