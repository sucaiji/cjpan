package com.sucaiji.cjpan.model.vo;

import com.sucaiji.cjpan.model.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVo {
    private Integer size;
    private Integer page;
    //总条数 用于前端显示总页数
    private Long total;
    private Integer pages;

    private List<Index> indexList;

    public PageVo(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }
}
