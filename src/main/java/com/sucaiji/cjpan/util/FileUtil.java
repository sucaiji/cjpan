package com.sucaiji.cjpan.util;

import com.sucaiji.cjpan.config.Property;
import com.sucaiji.cjpan.model.Range;
import com.sucaiji.cjpan.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileUtil {
    private final static Logger logger = LoggerFactory.getLogger(FileUtil.class);


    /**
     * 将传入的file写入到outputStream里面
     *
     * @param file
     * @param os
     * @throws IOException
     */
    public static void writeInOutputStream(File file, OutputStream os) throws IOException {
        if (file == null) {
            logger.error("writeInOutputStream错误，文件为空");
            throw new FileNotFoundException();
        }
        if (!file.exists()) {
            logger.error("writeInOutputStream错误，文件不存在，file路径为{}", file.getAbsolutePath());
            throw new FileNotFoundException();
        }
        byte[] buffer = new byte[1024];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)){
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        }
    }


    /**
     * 根据偏移量将数据写入流中
     *
     * @param file
     * @param os
     * @param range 偏移量
     * @throws IOException
     */
    public static void writeInOutputStream(File file, OutputStream os, Range range) throws IOException {
        if (file == null) {
            throw new FileNotFoundException();
        }
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        long limit = range.getLength();
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        try {
            System.out.println("偏移量" + range + "");
            randomAccessFile.seek(range.getStart());

            byte[] buffer = new byte[1024];
            int i = randomAccessFile.read(buffer);
            while (i != -1 && limit > 0) {
                os.write(buffer, 0, i);
                limit -= i;
                i = randomAccessFile.read(buffer);
            }
        } finally {
            randomAccessFile.close();
        }
    }

    /**
     * 通过uuid返回该文件的父路径的路径 文件的父路径是以该文件uuid值的前4位作为文件夹路径存放的
     *
     * @param uuid
     * @return
     */
    public static Path getFileParentPath(String uuid) {
        Path path = Paths.get(Property.DATA_DIR + File.separator + uuid.substring(0, 4));
        return path;
    }

    /**
     * 通过uuid返回该文件的路径
     *
     * @param uuid
     * @return
     */
    public static Path getFilePath(String uuid) {
        Path path = Paths.get(getFileParentPath(uuid).toString() + File.separator + uuid);
        return path;
    }

    /**
     * 通过uuid返回该文件的缩略图的路径
     *
     * @param uuid
     * @return
     */
    public static Path getFileThumbnailPath(String uuid) {
        StringBuilder stringBuilder = new StringBuilder(Property.THUMBNAIL_DIR)
                .append(File.separator).append(uuid, 0, 4)
                .append(File.separator).append(uuid).append(".jpg");
        Path path = Paths.get(stringBuilder.toString());
        return path;
    }

    /**
     * 根据传入的地址，删除文件，如果是文件夹的话，则同时删除文件夹下全部文件
     * @param fileStr
     */
    public static void deleteFile(String fileStr) {
        deleteFile(new File(fileStr));
    }

    /**
     * 根据传入的地址，删除文件，如果是文件夹的话，则同时删除文件夹下全部文件
     * @param filePath
     */
    public static void deleteFile(Path filePath) {
        File file = filePath.toFile();
        deleteFile(file);
    }

    /**
     * 根据传入的地址，删除文件，如果是文件夹的话，则同时删除文件夹下全部文件
     * @param file
     */
    public static void deleteFile(File file) {
        if (!file.exists()) {
            logger.error("删除文件失败：文件不存在！");
            return;
        }
        try {
            //先删除文件夹下所有东西
            String[] children = file.list();
            for (String str: children) {
                Files.delete(Paths.get(file.getAbsolutePath() + File.separator + str));
            }
            Files.delete(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
