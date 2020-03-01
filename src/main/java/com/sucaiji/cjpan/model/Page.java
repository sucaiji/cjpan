package com.sucaiji.cjpan.model;

import com.alibaba.fastjson.JSON;

public class Page {
    private Integer pg;
    private Integer limit;
    //总条数 用于前端显示总页数
    private Integer total;


    public Page() {
    }

    public Page(Integer pg) {
        this.pg = pg;
    }

    public Page(Integer pg, Integer limit) {
        this.pg = pg;
        this.limit = limit;
    }

    public Page(Integer pg, Integer limit, Integer total) {
        this.pg = pg;
        this.limit = limit;
        this.total = total;
    }

    public Integer getPg() {
        return pg;
    }

    public void setPg(Integer pg) {
        this.pg = pg;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
