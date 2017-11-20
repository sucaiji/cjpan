package com.sucaiji.cjpan.service.impl;


import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.dao.Md5Dao;
import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.file.FileOperate;
import com.sucaiji.cjpan.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;


@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private IndexDao indexDao;
    @Autowired
    private Md5Dao md5Dao;
    @Autowired
    private FileOperate fileOperate;

    @Override
    public void createDir(String name, String parentUuid) {
        String uuid=UUID();
        Timestamp time=time();

        Index index=new Index(uuid,parentUuid,name,true,time);
        indexDao.insertIndex(index);
    }

    @Override
    public List<Index> visitDir(String parentUuid) {
        Map<String,Object> map=new HashMap<>();
        map.put("parentUuid",parentUuid);
        return indexDao.selectIndex(map);
    }

    @Override
    public boolean saveFile(String parentUuid, String fileMd5,String name,int total) {
        String uuid=UUID();
        Timestamp time=time();
        String type=getType(name);
        Index index=new Index(uuid,parentUuid,name,type,false,time,6);
        //先文件的合并,与校验
        boolean checkSuccess=fileOperate.saveFile(fileMd5,total);
        if(!checkSuccess){
            return false;
        }
        //在文件树表中添加记录
        indexDao.insertIndex(index);
        //md5表中添加记录
        md5Dao.insert(fileMd5,uuid);
        return true;
    }

    @Override
    public void saveTemp(MultipartFile multipartFile,String fileMd5,String md5,Integer index) {
        fileOperate.tempSave(multipartFile,fileMd5,md5,index);
    }

    @Override
    public File getFileByUuid(String uuid) {

        return null;
    }

    @Override
    public boolean md5Exist(String md5) {
        List list=md5Dao.selectByMd5(md5);
        if (list.size()==0){
            return false;
        }
        return true;
    }

    /**
     *根据name解析出文件的类型
     * @param name
     * @return
     */
    private String getType(String name){
        return "mp3";
    }


    private String UUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    private Timestamp time(){
        return new Timestamp(System.currentTimeMillis());
    }
}
