package com.sucaiji.cjpan.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Index {
    private String uuid;
    private String parentUuid;
    private String name;
    private String suffix;
    private String type;
    private Boolean wasDir;
    private Timestamp lastUpdate;
    private Long size;

    public Index(String uuid, String parentUuid, String name, boolean wasDir, Timestamp lastUpdate) {
        this.uuid = uuid;
        this.parentUuid = parentUuid;
        this.name = name;
        this.wasDir = wasDir;
        this.lastUpdate = lastUpdate;
    }


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
