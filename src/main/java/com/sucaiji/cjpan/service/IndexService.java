package com.sucaiji.cjpan.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sucaiji.cjpan.config.Property;
import com.sucaiji.cjpan.config.TypeEnum;
import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.model.IndexModel;

import com.sucaiji.cjpan.model.Range;
import com.sucaiji.cjpan.model.vo.PageVo;
import com.sucaiji.cjpan.util.FileUtil;
import com.sucaiji.cjpan.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public PageVo getPageVo(Integer pg, Integer limit, IndexModel queryIndex) {
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
    public IndexModel getIndexByUuid(String uuid) {
        IndexModel indexModel = indexDao.get(uuid);
        return indexModel;
    }

    /**
     * 更新update数据，根据uuid更新，uuid不能为空
     * @param updateIndex
     */
    public void updateIndex(IndexModel updateIndex) {
        indexDao.updateIndex(updateIndex);
    }

    /**
     * 根据传入的index获取到文件缩略图，并写入到传入的outputstream里面
     * @param uuid
     * @param os
     * @throws IOException
     */
    public void writeThumbnailInOutputStream(String uuid, OutputStream os) throws IOException {
        File file = FileUtil.getFileThumbnailPath(uuid).toFile();
        FileUtil.writeInOutputStream(file, os);
    }


    /**
     * 根据传入的index获取到文件，并写入到传入的outputstream里面
     *
     * @param index
     * @param os
     */
    public void writeInOutputStream(IndexModel index, OutputStream os) throws IOException {
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
    public void writeInOutputStream(IndexModel index, OutputStream os, Range range) throws IOException {
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

        IndexModel queryIndex = new IndexModel();
        queryIndex.setName(name);
        queryIndex.setParentUuid(parentUuid);

        List<IndexModel> list = indexDao.selectIndex(queryIndex);
        if (list.size() > 0) {
            return false;
        }
        IndexModel index = new IndexModel(uuid, parentUuid, name, true, time);
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
            for (int i = 1; i <= total; i++) {
                files.add(new File(Property.TEMP_DIR + File.separator + uuid + File.separator + i));
            }
            for (File file1: files) {
                FileInputStream fis = new FileInputStream(file1);
                FileChannel inFileChannel = fis.getChannel();
                inFileChannel.transferTo(0, file1.length(), outFileChannel);
                inFileChannel.close();
            }
            outFileChannel.close();
            fos.close();

            File dirFile = new File(FileUtil.getFileParentPath(uuid).toString());
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }

            //将文件挪到data文件夹中
            Path from = Paths.get(file.getAbsolutePath());
            Path to = Paths.get(dirFile.getAbsolutePath() + File.separator + uuid);
            Files.move(from, to, REPLACE_EXISTING, ATOMIC_MOVE);

            //验证文件是否与文件夹下现有文件重名，如果重名则修改文件名
            String newName = name;
            IndexModel queryIndex = new IndexModel();
            queryIndex.setParentUuid(parentUuid);
            queryIndex.setName(name);
            List<IndexModel> list = indexDao.selectIndex(queryIndex);
            if (list.size() > 0) {
                newName = judgeName(name, parentUuid);
            }

            //获得文件的大小
            Long size = FileUtil.getFilePath(uuid).toFile().length();
            //文件记录入库
            Timestamp time = Utils.time();
            String suffix = Utils.getSuffix(newName);
            TypeEnum type = TypeEnum.getTypeByFileName(newName);
            IndexModel index = new IndexModel(uuid, parentUuid, newName, suffix, type.toString(), false, time, size);
            indexDao.insertIndex(index);

            //生成缩略图
            type.generateThumbnail(uuid);
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
        File file = new File(FileUtil.getFilePath(uuid).toString());
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
        IndexModel index = indexDao.get(uuid);
        if (index == null) {
            return;
        }
        //如果index是一个文件夹的话，递归删除文件夹下所有文件
        if (index.getWasDir()) {
            IndexModel queryIndex = new IndexModel();
            queryIndex.setParentUuid(index.getUuid());
            List<IndexModel> childUuidList = indexDao.selectIndex(queryIndex);
            for (IndexModel item : childUuidList) {
                deleteByUuid(item.getUuid());
            }
            indexDao.deleteByUuid(uuid);
        } else {
            indexDao.deleteByUuid(uuid);
            Path filePath = FileUtil.getFileParentPath(uuid);
            FileUtil.deleteFile(filePath);
            logger.info("删除[{}]文件成功", filePath.toString());
            return;
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
            IndexModel index = new IndexModel();
            index.setParentUuid(parentUuid);
            index.setName(newName);
            List<IndexModel> list = indexDao.selectIndex(index);
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





}
