package com.sucaiji.cjpan.config;

import java.io.File;

public interface Property {
    //根目录
    String APP_DIR =  System.getProperty("user.dir") + File.separator + "cjpanData";
    //数据文件地址
    String DATA_DIR = APP_DIR + File.separator + "data";
    //上传暂存文件地址
    String TEMP_DIR = APP_DIR + File.separator + "temp";
    //TODO 忘了什么地址了一会看一下
    String FRAME_TEMP_DIR = APP_DIR + File.separator + "frameTemp";
    //缩略图地址
    String THUMBNAIL_DIR = APP_DIR + File.separator + "thumbnail";
    //外部配置文件地址
    String EXTERNAL_PROPERTIES = APP_DIR + File.separator + "external.properties";


    String ROOT = "root";

    String UUID = "uuid";


}
