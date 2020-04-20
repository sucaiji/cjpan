package com.sucaiji.cjpan.config;

import com.sucaiji.cjpan.util.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Type {
    VIDEO("video"),
    IMAGE("image"),
    DOCUMENT("doc"),
    MUSIC("music"),
    OTHER("other");

    private static Pattern videoPattern = Pattern.compile("(mp4|rm|rmvb|wmv|avi|3gp|mkv|mov|MP4|RM|RMVB|WMV|AVI|3GP|MKV|MOV)");
    private static Pattern imagePattern = Pattern.compile("(jpg|jpeg|png|gif|JPG|JPEG|PNG|GIF)");
    private static Pattern musicPattern = Pattern.compile("(mp3|wav|wma|MP3|WAV|WMA)");
    private static Pattern docPattern = Pattern.compile("txt|pdf|TEXT|PDF");

    private String name;

    Type(String name) {
        this.name = name;
    }

    /**
     *
     * @param str
     * @return
     */
    public static Type getType(String str){
        for (Type type: Type.values()) {
            if (type.name.equals(str)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * 根据文件名获取文件类型
     * @param fileName
     * @return
     */
    public static Type getTypeByFileName(String fileName){
        //判断该文件是否有后缀名
        //TODO 此处正则  可能会有类似 "file." 的文件名被误判
        String[] strs = fileName.split("\\.", -1);
        if (strs.length < 2) {
            return Type.OTHER;
        }

        //去掉点
        String suffix = Utils.getSuffix(fileName).replaceAll("\\.", "");
        //支持的文件类型
        Matcher videoMatcher = videoPattern.matcher(suffix);
        if (videoMatcher.matches()) {
            return Type.VIDEO;
        }
        Matcher imageMatcher = imagePattern.matcher(suffix);
        if (imageMatcher.matches()) {
            return Type.IMAGE;
        }
        Matcher musicMatcher = musicPattern.matcher(suffix);
        if (musicMatcher.matches()) {
            return Type.MUSIC;
        }
        Matcher docMatcher = docPattern.matcher(suffix);
        if (docMatcher.matches()) {
            return Type.DOCUMENT;
        }
        return Type.OTHER;
    }

    @Override
    public String toString() {
        return name;
    }
}
