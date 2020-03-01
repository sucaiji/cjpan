package com.sucaiji.cjpan.model.vo;

import com.sucaiji.cjpan.model.Index;

import java.util.List;

public class PageVo {
    private Integer size;
    private Integer page;
    //总条数 用于前端显示总页数
    private Long total;
    private Integer pages;

    private List<Index> indexList;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public List<Index> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<Index> indexList) {
        this.indexList = indexList;
    }
}
