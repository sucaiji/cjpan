package com.sucaiji.cjpan.model;

import lombok.Data;

@Data
public class Range {
    private Long start;
    private Long end;
    private Long length;
    private Long total;

    public Range(Long start, Long end, Long total) {
        this.start = start;
        this.end = end;
        this.length = end - start + 1;
        this.total = total;
    }

    /**
     * 获取http请求要求的偏移量
     * @param rangeStr
     * @param fileSize
     * @return
     */
    public static Range getRange(String rangeStr, Long fileSize) {
        rangeStr = rangeStr.replaceAll("bytes=", "");
        String[] strs = rangeStr.split("-", 2);
        Long start;
        if (strs[0] == null || strs[0].isEmpty()) {
            start = 0L;
        } else {
            start = Long.valueOf(strs[0]);
        }

        Long end;
        if (strs[1] == null || strs[1].isEmpty()) {
            end = fileSize - 1;
        } else {
            end = Long.valueOf(strs[1]);
        }
        return new Range(start, end, fileSize);
    }
}
