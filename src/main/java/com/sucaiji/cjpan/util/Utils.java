package com.sucaiji.cjpan.util;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    /**
     * 获取uuid
     * @return
     */
    public static String UUID() {
        return java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 获取文件的后缀名
     *
     * @param name
     * @return
     */
    public static String getSuffix(String name) {
        Pattern p = Pattern.compile("\\.\\w+$");
        Matcher m = p.matcher(name);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    /**
     * 获取当前时间
     * @return
     */
    public static Timestamp time() {
        return new Timestamp(System.currentTimeMillis());
    }

}
