package com.sucaiji.cjpan.file.impl;

import com.sucaiji.cjpan.file.FileOperate;
import com.sucaiji.cjpan.service.Md5Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Component
public class FileOperateImpl implements FileOperate {
    @Autowired
    private Md5Service md5Service;

    private Path basePath;
    private Path dataPath;
    private Path tempPath;


    public FileOperateImpl(){
        basePath= Paths.get(System.getProperty("user.dir")+File.separator+APP_NAME_EN);
        dataPath=Paths.get(basePath.toString()+File.separator+DATA_DIR);
        tempPath=Paths.get(basePath.toString()+File.separator+TEMP_DIR);

    }

    @Override
    public boolean saveFile(String fileMd5,int total) {
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

            boolean checkMd5=md5Service.md5CheckSum(file,fileMd5);
            if(checkMd5){
                String dirName=fileMd5.substring(0,4);
                File dirFile=new File(dataPath.toString()+File.separator+dirName);
                if(!dirFile.exists()){
                    dirFile.mkdir();
                }

                Path from=Paths.get(file.getAbsolutePath());
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

    @Override
    public void tempSave(MultipartFile multipartFile, String fileMd5 ,String md5,Integer index) {
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

}
