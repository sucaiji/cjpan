package com.sucaiji.cjpan.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileOperate {
    String APP_NAME_EN="cjpan";
    String DATA_DIR="data";
    String TEMP_DIR="temp";

    /**
     * 合并文件并校验md5，如果校验失败则返回false，如果成功就移动文件到data文件夹并删除源文件
     * @param fileMd5
     */
    boolean saveFile(String fileMd5,int total);

    /**
     * 保存临时文件
     * @param file
     * @param md5
     * @param index
     */
    void tempSave(MultipartFile file, String fileMd5,String md5 ,Integer index);

}
