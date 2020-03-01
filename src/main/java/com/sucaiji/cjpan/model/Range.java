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
}
