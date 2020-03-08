package com.sucaiji.cjpan.config;

import java.io.File;

public interface Property {
    //根目录
    String APP_DIR =  System.getProperty("user.dir") + File.separator + "cjpanData";
    //数据文件地址
    String DATA_DIR = APP_DIR + File.separator + "data";
    //上传暂存文件地址
    String TEMP_DIR = APP_DIR + File.separator + "temp";
    //缩略图暂存地址
    String FRAME_TEMP_DIR = APP_DIR + File.separator + "frameTemp";
    //缩略图地址
    String THUMBNAIL_DIR = APP_DIR + File.separator + "thumbnail";
    //外部配置文件地址
    String EXTERNAL_PROPERTIES = APP_DIR + File.separator + "external.properties";

    //默认分页页数
    Integer DEFAULT_PAGE_SIZE = 20;
    //默认分页页数字符串版
    String DEFAULT_PAGE_SIZE_STR = "20";

    String ROOT = "root";

    String UUID = "uuid";


}
