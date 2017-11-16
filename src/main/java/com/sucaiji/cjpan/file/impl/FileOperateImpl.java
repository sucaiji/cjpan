package com.sucaiji.cjpan.file.impl;

import com.sucaiji.cjpan.file.FileOperate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileOperateImpl implements FileOperate {
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
            System.out.println("小样 你成功了！！");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("合并文件并校验md5，如果校验失败则返回false，如果成功就移动文件到data文件夹并删除源文件");

        return true;
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
