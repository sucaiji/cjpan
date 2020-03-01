package com.sucaiji.cjpan.init;

import com.sucaiji.cjpan.config.Type;
import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.dao.InitDao;
import com.sucaiji.cjpan.model.Index;
import com.sucaiji.cjpan.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static com.sucaiji.cjpan.config.Property.*;

@Component
public class InitRunner implements ApplicationRunner {


    @Autowired
    private InitDao initDao;

    @Autowired
    private IndexDao indexDao;

    private String initPath;

    @Autowired
    private IndexService indexService;

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
        initThumbnail();
    }

    private void initTable(){
        initDao.createTableUsers();
        initDao.createTableIndexs();
    }

    /**
     * 初始化视频和图片的缩略图
     */
    private void initThumbnail() {
        List<Index> indexList = indexDao.selectIndex(new Index());
        for (Index index: indexList) {
            if (index.getType() != null && !index.getType().equals("")) {
                indexService.generateThumbnail(index.getUuid(), Type.getType(index.getType()));
            }
        }
    }
}
