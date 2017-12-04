package com.sucaiji.cjpan.service.impl;


import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.dao.Md5Dao;
import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.service.IndexService;
import com.sucaiji.cjpan.util.Md5Util;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sucaiji.cjpan.init.InitRunner.*;
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

    private static final String VIDEO="video";
    private static final String IMAGE ="image";
    private static final String MUSIC="music";
    private static final String DOCUMENT="doc";
    private static final String OTHER="other";

    private Path basePath;
    private Path dataPath;
    private Path tempPath;
    private Path thumbnailPath;

    public IndexServiceImpl(){
        basePath= Paths.get(System.getProperty("user.dir")+File.separator+APP_NAME_EN);
        dataPath=Paths.get(basePath.toString()+File.separator+DATA_DIR);
        tempPath=Paths.get(basePath.toString()+File.separator+TEMP_DIR);
        thumbnailPath=Paths.get(basePath.toString()+File.separator+THUMBNAIL_DIR);
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
    public String getMd5ByUuid(String uuid) {
        return md5Dao.selectMd5ByUuid(uuid);
    }

    @Override
    public File getThumbnailByMd5(String md5) {
        return getFileThumbnailPath(md5).toFile();
    }

    @Override
    public void writeInOutputStream(String uuid, OutputStream os) throws IOException {
        //通过uuid获取一个index实例，并通过这个实例获取文件名
        Index index=getIndexByUuid(uuid);
        if(index==null){
            return;
            //return "error获取文件名失败";
        }
        writeInOutputStream(index,os);

    }

    @Override
    public void writeInOutputStream(Index index, OutputStream os) throws IOException {
        String uuid=index.getUuid();
        File file=getFileByUuid(uuid);
        writeInOutputStream(file,os);
    }

    @Override
    public void writeInOutputStream(File file, OutputStream os) throws IOException {
        if(file==null) {
            throw new FileNotFoundException();
        }
        if(!file.exists()){
            throw new FileNotFoundException();
        }

        byte[] buffer=new byte[1024];
        FileInputStream fis = null;
        BufferedInputStream bis =null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        }finally {
            bis.close();
            fis.close();
        }
    }


    @Override
    public boolean saveFile(String parentUuid, String fileMd5,String name,int total) {
        String uuid=UUID();
        Timestamp time=time();
        String suffix=getSuffix(name);
        String type=getType(name);
        Index index=new Index(uuid,parentUuid,name,suffix,type,false,time,0);
        //先文件的合并,与校验
        boolean checkSuccess= saveFile2(fileMd5,total);
        if(!checkSuccess){
            return false;
        }
        //在文件树表中添加记录
        indexDao.insertIndex(index);
        //md5表中添加记录
        md5Dao.insert(fileMd5,uuid);
        //生成缩略图
        switch (type){
            case VIDEO:
                //balabala
                break;
            case IMAGE:
                generateImageThumbnail(fileMd5);
                break;
            default:
                break;
        }
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
    public void saveByMd5(String md5,String parentUuid,String name) {
        String uuid=UUID();
        //根据该md5获取一个uuid列表
        List<String> list=md5Dao.selectUuidByMd5(md5);
        //根据任意一个uuid获取该index实例
        Index anotherIndex=getIndexByUuid(list.get(0));

        //获取一个index实例，就为了得到它的size，没有什么卵用，或许我应该换一种方式得到size？
        Index index=new Index(uuid,parentUuid,name,getSuffix(name),getType(name),false,time(),anotherIndex.getSize());
        indexDao.insertIndex(index);

        md5Dao.insert(md5,uuid);
    }

    @Override
    public File getFileByUuid(String uuid) {
        String md5=md5Dao.selectMd5ByUuid(uuid);
        if(md5==null){
            return null;
        }
        File file=new File(getFilePath(md5).toString());
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
        File parentFile=new File(getFileParentPath(fileMd5).toString());
        System.out.println(parentFile.getAbsolutePath());
        if(!parentFile.exists()){
            return;
        }

        //回头把这个复制的封装成一个方法
        try{
            //先删除文件夹下所有东西
            String[] children = parentFile.list();
            for (String str:children) {
                Files.delete(Paths.get(parentFile.getAbsolutePath()+File.separator+str));
            }
            //再删除文件夹本身   这两段写的很吉儿不严谨，也很不可读，回头重新写一遍
            Files.delete(Paths.get(parentFile.getAbsolutePath()));
        }catch (IOException e) {
            e.printStackTrace();
        }



    }

    /**
     *根据name解析出文件的类型
     * @param name
     * @return
     */
    private String getType(String name){
        //去掉点
        String suffix=getSuffix(name).replaceAll("\\.","");
        //支持的文件类型
        Pattern videoPattern = Pattern.compile("(mp4|rm|rmvb|wmv|avi|3gp|mkv)");
        Pattern imagePattern = Pattern.compile("(jpg|jpeg|png|gif)");
        Pattern musicPattern = Pattern.compile("(mp3|wav|wma)");
        Pattern docPattern = Pattern.compile("txt|pdf");
        Matcher videoMatcher = videoPattern.matcher(suffix);
        if(videoMatcher.matches()){
            return VIDEO;
        }
        Matcher imageMatcher = imagePattern.matcher(suffix);
        if(imageMatcher.matches()){
            return IMAGE;
        }
        Matcher musicMatcher = musicPattern.matcher(suffix);
        if(musicMatcher.matches()){
            return MUSIC;
        }
        Matcher docMatcher = docPattern.matcher(suffix);
        if(docMatcher.matches()){
            return DOCUMENT;
        }
        return OTHER;
    }

    /**
     * 获取文件的后缀名
     * @param name
     * @return
     */
    private String getSuffix(String name){
        Pattern p = Pattern.compile("\\.\\w+$");
        Matcher m = p.matcher(name);
        if(m.find()){
            return m.group();
        }
        return null;
    }
    /**
     * 通过md5返回该文件的父路径的路径 文件的父路径是以该文件md5值的前4位作为文件夹路径存放的
     * @param fileMd5
     * @return
     */
    private Path getFileParentPath(String fileMd5){
        Path path=Paths.get(dataPath.toString()+File.separator+fileMd5.substring(0,4));
        return path;
    }

    /**
     * 通过md5返回该文件的路径
     * @param fileMd5
     * @return
     */
    private Path getFilePath(String fileMd5){
        Path path=Paths.get(getFileParentPath(fileMd5).toString()+File.separator+fileMd5);
        return path;
    }

    /**
     * 通过md5返回该文件的缩略图的路径
     * @param md5
     * @return
     */
    private Path getFileThumbnailPath(String md5){
        Path path=Paths.get(thumbnailPath+File.separator+md5.substring(0,4)+File.separator+md5+".jpg");
        return path;
    }

    /**
     * 和上面的saveFile重名了。。想不出好名字。先起saveFile2回头再说,或者把俩个方法合一起
     * @param fileMd5
     * @param total
     * @return
     */
    private boolean saveFile2(String fileMd5, int total) {
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
                File dirFile=new File(getFileParentPath(fileMd5).toString());
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

    /**
     * 生成缩略图并存储
     */
    private void generateImageThumbnail(String md5){
        //创建路径
        File thumbnailFile=getFileThumbnailPath(md5).toFile();
        if(!thumbnailFile.exists()){
            thumbnailFile.getParentFile().mkdirs();
        }
        Thumbnails.Builder<BufferedImage> builder = null;
        try {
            BufferedImage image = ImageIO.read(getFilePath(md5).toFile());
            int height=image.getHeight();
            int width=image.getWidth();
            if(height>width){
                image=Thumbnails.of(image)
                        .width(256)
                        .asBufferedImage();
            }else{
                image=Thumbnails.of(image)
                        .height(256)
                        .asBufferedImage();
            }
            builder=Thumbnails.of(image).sourceRegion(Positions.CENTER,256,256).size(256,256);
            builder.outputFormat("jpg").toFile(getFileThumbnailPath(md5).toFile());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
