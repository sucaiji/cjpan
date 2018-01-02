package com.sucaiji.cjpan.init;

import com.sucaiji.cjpan.dao.InitDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

import static com.sucaiji.cjpan.config.Property.*;

@Component
public class InitRunner implements ApplicationRunner {
    @Autowired
    private InitDao initDao;

    private String  initPath;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {

        initPath=System.getProperty("user.dir");
        File file=new File(initPath+File.separator+ APP_NAME_EN);
        if(!file.exists()){
            file.mkdir();
        }
        File data=new File(file.getAbsolutePath()+File.separator+DATA_DIR);
        if(!data.exists()){
            data.mkdir();
        }
        File temp=new File(file.getAbsolutePath()+File.separator+TEMP_DIR);
        if(!temp.exists()){
            temp.mkdir();
        }
        File thumbnail=new File(file.getAbsolutePath()+File.separator+THUMBNAIL_DIR);
        if(!thumbnail.exists()){
            thumbnail.mkdir();
        }
        initTable();
    }

    private void initTable(){
        initDao.createTableUsers();
        initDao.createTableIndexs();
        initDao.createTableMd5();

    }
}
