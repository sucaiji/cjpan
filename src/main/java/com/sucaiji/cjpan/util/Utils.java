package com.sucaiji.cjpan.util;

public class Utils {
    public static String UUID() {
        return java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }
}
