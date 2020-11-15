package com.sucaiji.cjpan.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 */
@Data
public class FileNodeVo {

    private String name;

    private String uuid;

    private Boolean isParent;

    private List<FileNodeVo> childNodes;

    public FileNodeVo() {
    }

    public FileNodeVo(String name, String uuid, Boolean isParent, List<FileNodeVo> childNodes) {
        this.name = name;
        this.uuid = uuid;
        this.isParent = isParent;
        this.childNodes = childNodes;
    }
}
