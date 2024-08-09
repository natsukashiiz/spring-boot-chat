package com.natsukashiiz.sbchat.exception;

public class FileException extends BaseException {

    public FileException(String code) {
        super("file." + code);
    }

    public static FileException empty() {
        return new FileException("empty");
    }

    public static FileException unknown() {
        return new FileException("unknown");
    }

    public static BaseException notFound() {
        return new FileException("not.found");
    }

    public static BaseException typeNotSupported() {
        return new FileException("type.not.supported");
    }

    public static BaseException invalidUrl() {
        return new FileException("invalid.url");
    }
}
