package com.sucaiji.cjpan.util;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Map;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileUtil {

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
