package com.sucaiji.cjpan.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sucaiji.cjpan.config.Property;
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

    private final static Logger logger = LoggerFactory.getLogger(IndexService.class);


    @Autowired
    private IndexDao indexDao;

    private Map<String, Object> checkMap = new HashMap<>();
    private ExecutorService checkPool = Executors.newCachedThreadPool();

    /**
     * 获取文件列表vo
     * @param pg
     * @param limit
     * @param queryIndex
     * @return
     */
    public PageVo getPageVo(Integer pg, Integer limit, Index queryIndex) {
        if (null == limit || limit.equals(0)) {
            limit = DEFAULT_PAGE_SIZE;
        }
        if (null == pg || pg.equals(0)) {
            pg = 1;
        }

        PageHelper.startPage(pg, limit);
        Page page = (Page) indexDao.selectIndex(queryIndex);

        PageVo pageVo = new PageVo(pg, limit);
        pageVo.setPages(page.getPages());
        pageVo.setTotal(page.getTotal());
        pageVo.setIndexList(page.getResult());
        return pageVo;
    }

    /**
     * 分页搜索
     * @param pg
     * @param limit
     * @param name
     * @return
     */
    public PageVo search(Integer pg, Integer limit, String name) {
        if (null == limit || limit == 0) {
            limit = DEFAULT_PAGE_SIZE;
        }
        if (null == pg || pg == 0) {
            pg = 1;
        }
        PageHelper.startPage(pg, limit);
        Page page = (Page) indexDao.fuzzySelectIndex(name);

        PageVo pageVo = new PageVo(pg, limit);
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
        Index indexModel = indexDao.get(uuid);
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
        Timestamp time = Utils.time();

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


    //TODO 加上事务  回头测试是否实现了事务特性 (事务是否有用 毕竟文件删除可能会失败)
    @Transactional
    public void saveFile(String parentUuid, String uuid, String name, int total) {
        // File:文件分片路径/uuid，用来暂存合并后的总文件
        File file = new File(Property.TEMP_DIR + File.separator + uuid + File.separator + uuid);
        if (!file.getParentFile().exists()) {
            //如果temp文件夹不存在 则抛出异常或return
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(file);
             FileChannel outFileChannel = fos.getChannel()) {
            //将文件合并
            List<File> files = new ArrayList<>();
            for (int i = 0; i < total; i++) {
                files.add(new File(Property.TEMP_DIR + File.separator + uuid + File.separator + i));
            }
            for (File file1: files) {
                FileInputStream fis = new FileInputStream(file1);
                FileChannel inFileChannel = fis.getChannel();
                inFileChannel.transferTo(0, file1.length(), outFileChannel);
                inFileChannel.close();
            }

            File dirFile = new File(getFileParentPath(uuid).toString());
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }

            //将文件挪到data文件夹中
            Path from = Paths.get(file.getAbsolutePath());
            Path to = Paths.get(dirFile.getAbsolutePath() + File.separator + uuid);
            Files.move(from, to, REPLACE_EXISTING, ATOMIC_MOVE);

            //验证文件是否与文件夹下现有文件重名，如果重名则修改文件名
            String newName = name;
            Index queryIndex = new Index();
            queryIndex.setParentUuid(parentUuid);
            queryIndex.setName(name);
            List<Index> list = indexDao.selectIndex(queryIndex);
            if (list.size() > 0) {
                newName = judgeName(name, parentUuid);
            }

            //获得文件的大小
            Long size = getFilePath(uuid).toFile().length();
            //文件记录入库
            Timestamp time = Utils.time();
            String suffix = Utils.getSuffix(newName);
            Type type = Type.getTypeByFileName(newName);
            Index index = new Index(uuid, parentUuid, newName, suffix, type.toString(), false, time, size);
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
            //删除全部temp文件外加temp文件夹本身
            File dirFile = file.getParentFile();
            FileUtil.deleteFile(dirFile);
        }

    }



    public Map<String, Object> getCheckMap() {
        return checkMap;
    }

    /**
     * 检查文件是否上传完毕
     * @param uuid
     * @return
     */
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
            File tempDir = new File(Property.TEMP_DIR + File.separator + fileMd5);
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
        Index index = indexDao.get(uuid);
        if (index == null) {
            return;
        }
        //如果index是一个文件夹的话，递归删除文件夹下所有文件
        if (index.getWasDir()) {
            Index queryIndex = new Index();
            queryIndex.setParentUuid(index.getUuid());
            List<Index> childUuidList = indexDao.selectIndex(queryIndex);
            for (Index item : childUuidList) {
                deleteByUuid(item.getUuid());
            }
            indexDao.deleteByUuid(uuid);
        } else {
            indexDao.deleteByUuid(uuid);
            Path filePath = getFileParentPath(uuid);
            FileUtil.deleteFile(filePath);
            logger.info("删除[{}]文件成功", filePath.toString());
            return;
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
        File tempFile = new File(Property.FRAME_TEMP_DIR + File.separator + md5);
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
        Path path = Paths.get(Property.DATA_DIR + File.separator + uuid.substring(0, 4));
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
        StringBuilder stringBuilder = new StringBuilder(Property.THUMBNAIL_DIR)
                .append(File.separator).append(uuid, 0, 4)
                .append(File.separator).append(uuid).append(".jpg");
        Path path = Paths.get(stringBuilder.toString());
        return path;
    }



}
