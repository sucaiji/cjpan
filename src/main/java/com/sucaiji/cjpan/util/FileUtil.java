package com.sucaiji.cjpan.util;

import com.sucaiji.cjpan.model.Range;
import com.sucaiji.cjpan.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
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
//        FileInputStream fis = null;
//        BufferedInputStream bis = null;
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);){
//            fis = new FileInputStream(file);
//            bis = new BufferedInputStream(fis);
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


//    public static void copyFile() {
//        Path from = Paths.get(file.getAbsolutePath());
//        Path to = Paths.get(dirFile.getAbsolutePath() + File.separator + uuid);
//        Files.move(from, to, REPLACE_EXISTING, ATOMIC_MOVE);
//    }


//    public void saveFile(String tempPath, String parentUuid, String uuid, String name, int total) {
//        // File:文件分片路径/uuid，用来暂存合并后的总文件
//        File file = new File(tempPath.toString() + File.separator + uuid + File.separator + uuid);
//        if (!file.getParentFile().exists()) {
//            //如果temp文件夹不存在 则抛出异常或return
//            return;
//        }
//        try {
//            //将文件合并
//            FileOutputStream fos = new FileOutputStream(file);
//            FileChannel outFileChannel = fos.getChannel();
//
//            List<File> files = new ArrayList<>();
//            for (int i = 1; i <= total; i++) {
//                files.add(new File(tempPath.toString() + File.separator + uuid + File.separator + i));
//            }
//            for (File file1: files) {
//                FileInputStream fis = new FileInputStream(file1);
//                FileChannel inFileChannel = fis.getChannel();
//                inFileChannel.transferTo(0, file1.length(), outFileChannel);
//                inFileChannel.close();
//            }
//            outFileChannel.close();
//
//            File dirFile = new File(getFileParentPath(uuid).toString());
//            if (!dirFile.exists()) {
//                dirFile.mkdir();
//            }
//
//            Path from = Paths.get(file.getAbsolutePath());
//            Path to = Paths.get(dirFile.getAbsolutePath() + File.separator + uuid);
//            Files.move(from, to, REPLACE_EXISTING, ATOMIC_MOVE);
//
//            //获得文件的大小
//            Long size = getFilePath(uuid).toFile().length();
//
//            String newName = name;
//            Map<String, Object> map = new HashMap<>();
//            map.put(PARENT_UUID, parentUuid);
//            map.put(NAME, name);
//            List<Index> list = indexDao.selectIndex(map);
//            if (list.size() > 0) {
//                newName = judgeName(name, parentUuid);
//            }
//
//            Timestamp time = time();
//            String suffix = getSuffix(newName);
//            Type type = getType(newName);
//            Index index = new Index(uuid, parentUuid, newName, suffix, type.toString(), false, time, size);
//            //先文件的合并,与校验
//
//            //在文件树表中添加记录
//            indexDao.insertIndex(index);
//            //生成缩略图
//            generateThumbnail(uuid, type);
//            if (checkMap.containsKey(uuid)) {
//                checkMap.remove(uuid);
//            }
//
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            //TODO 回头新建一个FileSaveException 此方法出错就抛这个异常  由controller 来处理异常
//            //删除temp里面的文件
//
//            //先删除文件夹下所有东西
//            String[] children = file.getParentFile().list();
//            for (String str : children) {
//                try {
//                    Files.delete(Paths.get(file.getParentFile().getAbsolutePath() + File.separator + str));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//            //再删除文件夹本身
//            try {
//                Files.delete(Paths.get(file.getParentFile().getAbsolutePath()));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
}
