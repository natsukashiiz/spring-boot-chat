package com.natsukashiiz.sbchat.utils;

import java.util.regex.Pattern;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static final String USERNAME_REGEXP = "([a-zA-Z0-9_]{4,16})";
    public static final String MOBILE_REGEXP = "^(0[689]{1})\\d{8}$";

    public static boolean crossRange(String str, int min, int max) {
        int length = str.length();
        return length < min || length > max;
    }

    public static boolean invalidUsername(String value) {
        return !Pattern.matches(USERNAME_REGEXP, value);
    }

    public static boolean invalidMobile(String value) {
        return !Pattern.matches(MOBILE_REGEXP, value);
    }
}
