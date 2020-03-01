package com.sucaiji.cjpan.model;

import com.alibaba.fastjson.JSON;

import java.sql.Timestamp;

public class Index {
    private String uuid;
    private String parentUuid;
    private String name;
    private String suffix;
    private String type;
    private boolean wasDir;
    private Timestamp lastUpdate;
    private Long size;

    public Index() {
    }

    public Index(String uuid, String parentUuid, String name,String suffix ,String type, boolean wasDir, Timestamp lastUpdate, Long size) {
        this.uuid = uuid;
        this.parentUuid = parentUuid;
        this.name = name;
        this.suffix = suffix;
        this.type = type;
        this.wasDir = wasDir;
        this.lastUpdate = lastUpdate;
        this.size = size;
    }


    public Index(String uuid, String parentUuid, String name, boolean wasDir, Timestamp lastUpdate) {
        this.uuid = uuid;
        this.parentUuid = parentUuid;
        this.name = name;
        this.wasDir = wasDir;
        this.lastUpdate = lastUpdate;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(String parentUuid) {
        this.parentUuid = parentUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getWasDir() {
        return wasDir;
    }

    public void setWasDir(boolean wasDir) {
        this.wasDir = wasDir;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
