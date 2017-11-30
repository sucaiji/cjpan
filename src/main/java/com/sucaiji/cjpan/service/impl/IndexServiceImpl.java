package com.sucaiji.cjpan.service.impl;


import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.dao.Md5Dao;
import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.service.IndexService;
import com.sucaiji.cjpan.util.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;

import static com.sucaiji.cjpan.init.InitRunner.APP_NAME_EN;
import static com.sucaiji.cjpan.init.InitRunner.DATA_DIR;
import static com.sucaiji.cjpan.init.InitRunner.TEMP_DIR;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private IndexDao indexDao;
    @Autowired
    private Md5Dao md5Dao;

    @Autowired
    private Md5Util md5Util;

    private Path basePath;
    private Path dataPath;
    private Path tempPath;

    public IndexServiceImpl(){
        basePath= Paths.get(System.getProperty("user.dir")+File.separator+APP_NAME_EN);
        dataPath=Paths.get(basePath.toString()+File.separator+DATA_DIR);
        tempPath=Paths.get(basePath.toString()+File.separator+TEMP_DIR);
    }

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
    public Index getIndexByUuid(String uuid) {
        Map<String,Object> map=new HashMap<>();
        map.put("uuid",uuid);
        List<Index> list=indexDao.selectIndex(map);
        if(list.size()==0) {
            return null;
        }
        Index index =  list.get(0);
        return index;
    }


    @Override
    public boolean saveFile(String parentUuid, String fileMd5,String name,int total) {
        String uuid=UUID();
        Timestamp time=time();
        String type=getType(name);
        Index index=new Index(uuid,parentUuid,name,type,false,time,6);
        //先文件的合并,与校验
        boolean checkSuccess=saveFile(fileMd5,total);
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
        try {
            File tempDir=new File(tempPath.toFile()+File.separator+fileMd5);
            if(!tempDir.exists()){
                tempDir.mkdir();
            }
            File file=new File(tempDir.getAbsolutePath()+File.separator+index);
            multipartFile.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getFileByUuid(String uuid) {
        String md5=md5Dao.selectMd5ByUuid(uuid);
        if(md5==null){
            return null;
        }
        File file=new File(getMd5SubString(md5)+File.separator+md5);
        if(!file.exists()){
            return null;
        }
        return file;
    }

    @Override
    public boolean md5Exist(String md5) {
        List list=md5Dao.selectUuidByMd5(md5);
        if (list.size()==0){
            return false;
        }
        return true;
    }

    @Override
    public void deleteByUuid(String uuid) {
        Map<String,Object> map=new HashMap();
        map.put("uuid",uuid);
        List<Index> list=indexDao.selectIndex(map);
        if(list.size()==0){
            return;
        }

        Index index=list.get(0);

        //如果index是一个文件夹的话，递归删除文件夹下所有文件
        if(index.getWasDir()){
            System.out.println("是文件夹，递归删除");
            Map<String,Object> map1=new HashMap();
            map1.put("parentUuid",index.getUuid());
            List<Index> childUuidList=indexDao.selectIndex(map1);
            for (Index index1:childUuidList){
                deleteByUuid(index1.getUuid());
            }
        }else {
            //如果不是文件夹 则执行删除数据库信息，判断md5表中是否还有该md5记录，如果没有了，则删除该文件
            String md5=md5Dao.selectMd5ByUuid(uuid);

            md5Dao.deleteByUuid(uuid);

            Map<String,Object> map2=new HashMap<>();
            map2.put("uuid",uuid);
            indexDao.deleteIndex(map2);

            //查询md5表中是否还有该md5的记录,如果没有的话，删除文件
            List<String> list1=md5Dao.selectUuidByMd5(md5);
            if(list1.size()==0){
                deleteFile(md5);
                System.out.println("文件删除");
                return;
            }




        }

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

    /**
     * 根据md5值删除文件
     * @param fileMd5
     */
    private void deleteFile(String fileMd5){
        File file=new File(getMd5SubString(fileMd5));
        System.out.println(file.getAbsolutePath());
        if(!file.exists()){
            return;
        }

        //回头把这个复制的封装成一个方法
        try{
            //先删除文件夹下所有东西
            String[] children = file.list();
            for (String str:children) {
                Files.delete(Paths.get(file.getAbsolutePath()+File.separator+str));
            }
            //再删除文件夹本身   这两段写的很吉儿不严谨，也很不可读，回头重新写一遍
            Files.delete(Paths.get(file.getAbsolutePath()));
        }catch (IOException e) {
            e.printStackTrace();
        }



    }

    /**
     * 返回该文件的路径 文件的存放路径是以该文件md5值的前4位作为文件夹路径存放的
     * @param fileMd5
     * @return
     */
    private String getMd5SubString(String fileMd5){
        Path path=Paths.get(dataPath.toString()+File.separator+fileMd5.substring(0,4));
        return path.toString();
    }

    private boolean saveFile(String fileMd5,int total) {
        //回头重写一遍可读性高的
        File file=new File(tempPath.toString()+File.separator+fileMd5+File.separator+fileMd5);
        if(!file.getParentFile().exists()){
            return false;
        }
        try {
            FileOutputStream fos=new FileOutputStream(file);
            FileChannel outFileChannel=fos.getChannel();

            List<File> files=new ArrayList<>();
            for(int i=1;i<=total;i++){
                files.add(new File(tempPath.toString()+File.separator+fileMd5+File.separator+i));
            }
            for(File file1:files){
                FileInputStream fis=new FileInputStream(file1);
                FileChannel inFileChannel=fis.getChannel();
                inFileChannel.transferTo(0,file1.length(),outFileChannel);
                inFileChannel.close();
            }
            outFileChannel.close();

            boolean checkMd5= md5Util.md5CheckSum(file,fileMd5);
            if(checkMd5){
                File dirFile=new File(getMd5SubString(fileMd5));
                if(!dirFile.exists()){
                    dirFile.mkdir();
                }

                Path from= Paths.get(file.getAbsolutePath());
                Path to=Paths.get(dirFile.getAbsolutePath()+File.separator+fileMd5);
                Files.move(from, to, REPLACE_EXISTING, ATOMIC_MOVE);
                //删除temp里面的文件

                //先删除文件夹下所有东西
                String[] children = file.getParentFile().list();
                for (String str:children) {
                    Files.delete(Paths.get(file.getParentFile().getAbsolutePath()+File.separator+str));

                }

                //再删除文件夹本身   这两段写的很吉儿不严谨，也很不可读，回头重新写一遍
                Files.delete(Paths.get(file.getParentFile().getAbsolutePath()));
                return true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        System.out.println("合并文件并校验md5，如果校验失败则返回false，如果成功就移动文件到data文件夹并删除源文件");

        return false;
    }
}
