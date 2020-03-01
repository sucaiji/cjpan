package com.sucaiji.cjpan.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sucaiji.cjpan.config.Type;
import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.model.Index;

import com.sucaiji.cjpan.model.Range;
import com.sucaiji.cjpan.model.vo.PageVo;
import com.sucaiji.cjpan.util.FileUtil;
import com.sucaiji.cjpan.util.Utils;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sucaiji.cjpan.config.Property.*;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class IndexService {
    //默认每页数量
    public static final Integer DEFAULT_PAGE_SIZE=200;

    final static Logger logger = LoggerFactory.getLogger(IndexService.class);

    @Autowired
    private IndexDao indexDao;

    private Map<String, Object> checkMap = new HashMap<>();
    private ExecutorService checkPool = Executors.newCachedThreadPool();

    private Path basePath;
    private Path dataPath;
    private Path tempPath;
    private Path thumbnailPath;
    private Path frameTempPath;

    public IndexService() {
        basePath = Paths.get(System.getProperty("user.dir") + File.separator + APP_NAME_EN);
        dataPath = Paths.get(basePath.toString() + File.separator + DATA_DIR);
        tempPath = Paths.get(basePath.toString() + File.separator + TEMP_DIR);
        thumbnailPath = Paths.get(basePath.toString() + File.separator + THUMBNAIL_DIR);
        frameTempPath = Paths.get(basePath.toString() + File.separator + FRAME_TEMP_DIR);
    }


    /**
     * 获取文件列表vo
     * @param pg
     * @param limit
     * @param queryIndex
     * @return
     */
    public PageVo getPageVo(Integer pg, Integer limit, Index queryIndex) {
        if (null == limit || limit == 0) {
            limit = DEFAULT_PAGE_SIZE;
        }
        if (null == pg || pg == 0) {
            pg = 1;
        }

        PageHelper.startPage(pg, limit);
        Page page = (Page) indexDao.selectIndex(queryIndex);

        PageVo pageVo = new PageVo();
        pageVo.setSize(limit);
        pageVo.setPage(pg);
        pageVo.setPages(page.getPages());
        pageVo.setTotal(page.getTotal());
        pageVo.setIndexList(page.getResult());
        return pageVo;
    }

    /**
     * 通过uuid获取index数据
     * @param uuid
     * @return
     */
    public Index getIndexByUuid(String uuid) {
        Index index = new Index();
        index.setUuid(uuid);
        List<Index> list = indexDao.selectIndex(index);
        if (list.size() == 0) {
            return null;
        }
        Index indexModel = list.get(0);
        return indexModel;
    }

    /**
     * 更新update数据，根据uuid更新，uuid不能为空
     * @param updateIndex
     */
    public void updateIndex(Index updateIndex) {
        indexDao.updateIndex(updateIndex);
    }

    /**
     * 根据传入的index获取到文件缩略图，并写入到传入的outputstream里面
     * @param uuid
     * @param os
     * @throws IOException
     */
    public void writeThumbnailInOutputStream(String uuid, OutputStream os) throws IOException {
        File file = getFileThumbnailPath(uuid).toFile();
        FileUtil.writeInOutputStream(file, os);
    }


    /**
     * 根据传入的index获取到文件，并写入到传入的outputstream里面
     *
     * @param index
     * @param os
     */
    public void writeInOutputStream(Index index, OutputStream os) throws IOException {
        String uuid = index.getUuid();
        File file = getFileByUuid(uuid);
        FileUtil.writeInOutputStream(file, os);
    }

    /**
     * 根据传入的index获取到文件，并根据偏移量写入到传入的outputstream里面
     * @param index
     * @param os
     * @param range
     * @throws IOException
     */
    public void writeInOutputStream(Index index, OutputStream os, Range range) throws IOException {
        String uuid = index.getUuid();
        File file = getFileByUuid(uuid);
        FileUtil.writeInOutputStream(file, os, range);
    }

    /**
     * 创建文件夹
     * @param name
     * @param parentUuid
     * @return 文件夹创建是否成功，如果文件同名则会失败
     */
    public boolean createDir(String name, String parentUuid) {
        String uuid = Utils.UUID();
        Timestamp time = time();

        Index queryIndex = new Index();
        queryIndex.setName(name);
        queryIndex.setParentUuid(parentUuid);

        List<Index> list = indexDao.selectIndex(queryIndex);
        if (list.size() > 0) {
            return false;
        }
        Index index = new Index(uuid, parentUuid, name, true, time);
        indexDao.insertIndex(index);
        return true;
    }


    /**
     * 获取http请求要求的偏移量
     * @param rangeStr
     * @param fileSize
     * @return
     */
    public Range getRange(String rangeStr, Long fileSize) {
        rangeStr = rangeStr.replaceAll("bytes=", "");
        String[] strs = rangeStr.split("-", 2);
        Long start;
        if (strs[0] == null || strs[0].isEmpty()) {
            start = 0L;
        } else {
            start = Long.valueOf(strs[0]);
        }

        Long end;
        if (strs[1] == null || strs[1].isEmpty()) {
            end = fileSize - 1;
        } else {
            end = Long.valueOf(strs[1]);
        }
        return new Range(start, end, fileSize);
    }

    //TODO 加上事务  回头测试是否实现了事务特性
    @Transactional
    public void saveFile(String parentUuid, String uuid, String name, int total) {
        // File:文件分片路径/uuid，用来暂存合并后的总文件
        File file = new File(tempPath.toString() + File.separator + uuid + File.separator + uuid);
        if (!file.getParentFile().exists()) {
            //如果temp文件夹不存在 则抛出异常或return
            return;
        }
        try {
            //将文件合并
            FileOutputStream fos = new FileOutputStream(file);
            FileChannel outFileChannel = fos.getChannel();

            List<File> files = new ArrayList<>();
            for (int i = 1; i <= total; i++) {
                files.add(new File(tempPath.toString() + File.separator + uuid + File.separator + i));
            }
            for (File file1: files) {
                FileInputStream fis = new FileInputStream(file1);
                FileChannel inFileChannel = fis.getChannel();
                inFileChannel.transferTo(0, file1.length(), outFileChannel);
                inFileChannel.close();
            }
            outFileChannel.close();

            File dirFile = new File(getFileParentPath(uuid).toString());
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }

            Path from = Paths.get(file.getAbsolutePath());
            Path to = Paths.get(dirFile.getAbsolutePath() + File.separator + uuid);
            Files.move(from, to, REPLACE_EXISTING, ATOMIC_MOVE);

            //获得文件的大小
            Long size = getFilePath(uuid).toFile().length();

            String newName = name;
            Index queryIndex = new Index();
            queryIndex.setParentUuid(parentUuid);
            queryIndex.setName(name);
            List<Index> list = indexDao.selectIndex(queryIndex);
            if (list.size() > 0) {
                newName = judgeName(name, parentUuid);
            }

            Timestamp time = time();
            String suffix = getSuffix(newName);
            Type type = getType(newName);
            Index index = new Index(uuid, parentUuid, newName, suffix, type.toString(), false, time, size);
            //先文件的合并,与校验

            //在文件树表中添加记录
            indexDao.insertIndex(index);
            //生成缩略图
            generateThumbnail(uuid, type);
            if (checkMap.containsKey(uuid)) {
                checkMap.remove(uuid);
            }



        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //TODO 回头新建一个FileSaveException 此方法出错就抛这个异常  由controller 来处理异常
            //删除temp里面的文件

            //先删除文件夹下所有东西
            String[] children = file.getParentFile().list();
            for (String str : children) {
                try {
                    Files.delete(Paths.get(file.getParentFile().getAbsolutePath() + File.separator + str));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            //再删除文件夹本身
            try {
                Files.delete(Paths.get(file.getParentFile().getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



    public Map<String, Object> getCheckMap() {
        return checkMap;
    }

    public boolean checkUpload(String uuid) {
        if (checkMap.containsKey(uuid)) {
            return false;
        }
        return true;
    }



    /**
     * 将接收到的分片包保存在临时文件夹
     *
     * @param multipartFile
     * @param fileMd5
     * @param index
     */
    public void saveTemp(MultipartFile multipartFile, String fileMd5, Integer index) {
        try {
            File tempDir = new File(tempPath.toFile() + File.separator + fileMd5);
            if (!tempDir.exists()) {
                tempDir.mkdir();
            }
            File file = new File(tempDir.getAbsolutePath() + File.separator + index);
            multipartFile.transferTo(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 通过uuid获取文件实际所在位置，如果文件不存在则返回null
     *
     * @param uuid
     * @return
     */
    public File getFileByUuid(String uuid) {
        File file = new File(getFilePath(uuid).toString());
        if (!file.exists()) {
            return null;
        }
        return file;
    }

    /**
     * 删除某个文件，如果该uuid指向一个文件夹的话，则删除该文件夹下所有文件
     *
     * @param uuid
     */
    public void deleteByUuid(String uuid) {
        Index index1 = new Index();
        index1.setUuid(uuid);
        List<Index> list = indexDao.selectIndex(index1);
        if (list.size() == 0) {
            return;
        }

        Index index = list.get(0);

        //如果index是一个文件夹的话，递归删除文件夹下所有文件
        if (index.getWasDir()) {
            Index queryIndex = new Index();
            queryIndex.setParentUuid(index.getUuid());
            List<Index> childUuidList = indexDao.selectIndex(queryIndex);
            for (Index item : childUuidList) {
                deleteByUuid(item.getUuid());
            }
            Map<String, Object> map2 = new HashMap<>();
            map2.put("uuid", uuid);
            indexDao.deleteIndex(map2);
        } else {
            //如果不是文件夹 则执行删除数据库信息，判断md5表中是否还有该md5记录，如果没有了，则删除该文件
//            String md5 = md5Dao.selectMd5ByUuid(uuid);

            Map<String, Object> map3 = new HashMap<>();
            map3.put("uuid", uuid);
            indexDao.deleteIndex(map3);

            //查询md5表中是否还有该md5的记录,如果没有的话，删除文件
//            List<String> list1 = md5Dao.selectUuidByMd5(md5);
//            if (list1.size() == 0) {
                deleteFile(uuid);
                System.out.println("文件删除");
                return;
//            }

        }
    }




    /**
     * 生成缩略图
     * @param md5
     * @param type
     */
    public void generateThumbnail(String md5, Type type) {
        switch (type) {
            case VIDEO:
                generateMovieTumbnail(md5);
                break;
            case IMAGE:
                generateImageThumbnail(md5);
                break;
            default:
                break;
        }
    }


    /**
     * 生成缩略图并存储
     */
    private void generateImageThumbnail(String md5) {
        //创建路径

        File thumbnailFile = getFileThumbnailPath(md5).toFile();
        //如果缩略图存在 则跳过
        if (thumbnailFile.exists()) {
            return;
        }

        if (!thumbnailFile.exists()) {
            thumbnailFile.getParentFile().mkdirs();
        }
        Thumbnails.Builder<BufferedImage> builder = null;
        try {
            BufferedImage image = ImageIO.read(getFilePath(md5).toFile());
            int height = image.getHeight();
            int width = image.getWidth();
            if (height > width) {
                image = Thumbnails.of(image)
                        .width(256)
                        .asBufferedImage();
            } else {
                image = Thumbnails.of(image)
                        .height(256)
                        .asBufferedImage();
            }
            builder = Thumbnails.of(image).sourceRegion(Positions.CENTER, 256, 256).size(256, 256);
            builder.outputFormat("jpg").toFile(getFileThumbnailPath(md5).toFile());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成视频的缩略图
     * @param md5
     */
    private void generateMovieTumbnail(String md5) {
        int frameNumber = new Random().nextInt(80000);
        File thumbnailFile = getFileThumbnailPath(md5).toFile();
        //如果缩略图已经存在 则返回
        if (thumbnailFile.exists()) {
            return;
        }
        File tempFile = new File(frameTempPath.toString() + File.separator + md5);
        if (!thumbnailFile.exists()) {
            thumbnailFile.getParentFile().mkdirs();
        }
        if (!tempFile.exists()) {
            tempFile.getParentFile().mkdirs();
        }
        try {
            Picture picture = FrameGrab.getFrameFromFile(getFilePath(md5).toFile(), frameNumber);
            //picture==null代表不支持此格式
            if (picture == null) {
                return;
            }
            BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
            ImageIO.write(bufferedImage, "jpg", tempFile);

            Thumbnails.Builder<BufferedImage> builder = null;
            BufferedImage image = ImageIO.read(tempFile);
            int height = image.getHeight();
            int width = image.getWidth();
            if (height > width) {
                image = Thumbnails.of(image)
                        .width(256)
                        .asBufferedImage();
            } else {
                image = Thumbnails.of(image)
                        .height(256)
                        .asBufferedImage();
            }
            builder = Thumbnails.of(image).sourceRegion(Positions.CENTER, 256, 256).size(256, 256);
            builder.outputFormat("jpg").toFile(thumbnailFile);

            Files.delete(tempFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JCodecException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static class Range {
//        public Long start;
//        public Long end;
//        public Long length;
//        public Long total;
//
//        public Range(Long start, Long end, Long total) {
//            this.start = start;
//            this.end = end;
//            this.length = end - start + 1;
//            this.total = total;
//        }
//    }


    private Timestamp time() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 根据uuid值删除文件
     *
     * @param uuid
     */
    private void deleteFile(String uuid) {
        File parentFile = new File(getFileParentPath(uuid).toString());
        System.out.println(parentFile.getAbsolutePath());
        if (!parentFile.exists()) {
            return;
        }

        //回头把这个复制的封装成一个方法
        try {
            //先删除文件夹下所有东西
            String[] children = parentFile.list();
            for (String str : children) {
                Files.delete(Paths.get(parentFile.getAbsolutePath() + File.separator + str));
            }
            //再删除文件夹本身   这两段写的很吉儿不严谨，也很不可读，回头重新写一遍
            Files.delete(Paths.get(parentFile.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private Pattern videoPattern = Pattern.compile("(mp4|rm|rmvb|wmv|avi|3gp|mkv|mov|MP4|RM|RMVB|WMV|AVI|3GP|MKV|MOV)");
    private Pattern imagePattern = Pattern.compile("(jpg|jpeg|png|gif|JPG|JPEG|PNG|GIF)");
    private Pattern musicPattern = Pattern.compile("(mp3|wav|wma|MP3|WAV|WMA)");
    private Pattern docPattern = Pattern.compile("txt|pdf|TEXT|PDF");
    /**
     * 根据name解析出文件的类型
     *
     * @param name
     * @return
     */
    private Type getType(String name) {

        //判断该文件是否有后缀名
        String[] strs = name.split("\\.", -1);
        if (strs.length < 2) {
            return Type.OTHER;
        }

        //去掉点
        String suffix = getSuffix(name).replaceAll("\\.", "");
        //支持的文件类型
        Matcher videoMatcher = videoPattern.matcher(suffix);
        if (videoMatcher.matches()) {
            return Type.VIDEO;
        }
        Matcher imageMatcher = imagePattern.matcher(suffix);
        if (imageMatcher.matches()) {
            return Type.IMAGE;
        }
        Matcher musicMatcher = musicPattern.matcher(suffix);
        if (musicMatcher.matches()) {
            return Type.MUSIC;
        }
        Matcher docMatcher = docPattern.matcher(suffix);
        if (docMatcher.matches()) {
            return Type.DOCUMENT;
        }
        return Type.OTHER;
    }

    /**
     * 获取文件的后缀名
     *
     * @param name
     * @return
     */
    private String getSuffix(String name) {
        Pattern p = Pattern.compile("\\.\\w+$");
        Matcher m = p.matcher(name);
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    /**
     * 循环判断是否重名 如果同名，添加后缀copy
     * @param name
     * @param parentUuid
     * @return
     */
    private String judgeName(String name, String parentUuid) {
        String newName = name;
        while (true) {
            Index index = new Index();
            index.setParentUuid(parentUuid);
            index.setName(newName);
            List<Index> list = indexDao.selectIndex(index);
            if (list.size() > 0) {
                newName = addCopy(newName);
            } else {
                break;
            }
        }
        return newName;
    }

    /**
     * 循环判断是否重名 如果同名，添加后缀copy
     * @param name
     * @return
     */
    private String addCopy(String name) {
        String[] strs = name.split("\\.", -1);
        StringBuilder newName = new StringBuilder();
        if (strs.length > 1) {
            for (int i = 0; i < strs.length - 1; i++) {
                newName.append(strs[i]);
            }
            newName.append(" - copy.");
            newName.append(strs[strs.length - 1]);
        } else {
            newName.append(name);
            newName.append(" - copy");
        }
        return newName.toString();
    }

    /**
     * 通过uuid返回该文件的父路径的路径 文件的父路径是以该文件uuid值的前4位作为文件夹路径存放的
     *
     * @param uuid
     * @return
     */
    private Path getFileParentPath(String uuid) {
        Path path = Paths.get(dataPath.toString() + File.separator + uuid.substring(0, 4));
        return path;
    }

    /**
     * 通过uuid返回该文件的路径
     *
     * @param uuid
     * @return
     */
    private Path getFilePath(String uuid) {
        Path path = Paths.get(getFileParentPath(uuid).toString() + File.separator + uuid);
        return path;
    }

    /**
     * 通过uuid返回该文件的缩略图的路径
     *
     * @param uuid
     * @return
     */
    private Path getFileThumbnailPath(String uuid) {
        Path path = Paths.get(thumbnailPath + File.separator + uuid.substring(0, 4) + File.separator + uuid + ".jpg");
        return path;
    }








}
