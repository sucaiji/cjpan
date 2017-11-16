package com.sucaiji.cjpan.init;

import com.sucaiji.cjpan.dao.InitDao;
import com.sucaiji.cjpan.file.FileOperate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class InitRunner implements ApplicationRunner {
    @Autowired
    private InitDao initDao;


    public static final String APP_NAME_EN="cjpan";
    public static final String DATA_DIR="data";
    public static final String TEMP_DIR="temp";
    private String  initPath;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        initPath=System.getProperty("user.dir");
        File file=new File(initPath+File.separator+ FileOperate.APP_NAME_EN);
        if(!file.exists()){
            file.mkdir();
        }
        File data=new File(file.getAbsolutePath()+File.separator+FileOperate.DATA_DIR);
        if(!data.exists()){
            data.mkdir();
        }
        File temp=new File(file.getAbsolutePath()+File.separator+FileOperate.TEMP_DIR);
        if(!temp.exists()){
            temp.mkdir();
        }
        initTable();
    }

    private void initTable(){
        initDao.createTableUsers();
        initDao.createTableIndexs();
        initDao.createTableMd5();
    }
}
