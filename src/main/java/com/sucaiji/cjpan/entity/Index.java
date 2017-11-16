package com.sucaiji.cjpan.entity;

import java.sql.Timestamp;

public class Index {
    private String uuid;
    private String parentUuid;
    private String name;
    private String type;
    private boolean wasDir;
    private Timestamp lastUpdate;
    private int size;

    public Index() {
    }

    public Index(String uuid, String parentUuid, String name, String type, boolean wasDir, Timestamp lastUpdate, int size) {
        this.uuid = uuid;
        this.parentUuid = parentUuid;
        this.name = name;
        this.type = type;
        this.wasDir = wasDir;
        this.lastUpdate = lastUpdate;
        this.size = size;
    }

    public Index(String uuid, String name, String type, boolean wasDir, Timestamp lastUpdate, int size) {
        this.uuid = uuid;
        this.name = name;
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

    public Index(String uuid, String name, boolean wasDir, Timestamp lastUpdate) {
        this.uuid = uuid;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }


}
