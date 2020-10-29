package com.sucaiji.cjpan.config;

import com.sucaiji.cjpan.util.FileUtil;
import com.sucaiji.cjpan.util.Utils;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TypeEnum {

    VIDEO("video", new VideoGeneratePolicy()),
    IMAGE("image", new ImageGeneratePolicy()),
    DOCUMENT("doc", uuid -> {}),
    MUSIC("music", uuid -> {}),
    OTHER("other", uuid -> {});

    private static Pattern videoPattern = Pattern.compile("(mp4|rm|rmvb|wmv|avi|3gp|mkv|mov|MP4|RM|RMVB|WMV|AVI|3GP|MKV|MOV)");
    private static Pattern imagePattern = Pattern.compile("(jpg|jpeg|png|gif|JPG|JPEG|PNG|GIF)");
    private static Pattern musicPattern = Pattern.compile("(mp3|wav|wma|MP3|WAV|WMA)");
    private static Pattern docPattern = Pattern.compile("txt|pdf|TEXT|PDF");

    private String name;

    private ThumbnailGeneratePolicy policy;

    TypeEnum(String name, ThumbnailGeneratePolicy policy) {
        this.name = name;
        this.policy = policy;
    }

    /**
     * 根据文件类型生成缩略图
     * @param uuid
     */
    public void generateThumbnail(String uuid) {
        this.policy.generate(uuid);
    }

    /**
     *
     * @param str
     * @return
     */
    public static TypeEnum getType(String str){
        for (TypeEnum type: TypeEnum.values()) {
            if (type.name.equals(str)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * 根据文件名获取文件类型
     * @param fileName
     * @return
     */
    public static TypeEnum getTypeByFileName(String fileName){
        //判断该文件是否有后缀名
        //TODO 此处正则  可能会有类似 "file." 的文件名被误判
        String[] strs = fileName.split("\\.", -1);
        if (strs.length < 2) {
            return TypeEnum.OTHER;
        }

        //去掉点
        String suffix = Utils.getSuffix(fileName).replaceAll("\\.", "");
        //支持的文件类型
        Matcher videoMatcher = videoPattern.matcher(suffix);
        if (videoMatcher.matches()) {
            return TypeEnum.VIDEO;
        }
        Matcher imageMatcher = imagePattern.matcher(suffix);
        if (imageMatcher.matches()) {
            return TypeEnum.IMAGE;
        }
        Matcher musicMatcher = musicPattern.matcher(suffix);
        if (musicMatcher.matches()) {
            return TypeEnum.MUSIC;
        }
        Matcher docMatcher = docPattern.matcher(suffix);
        if (docMatcher.matches()) {
            return TypeEnum.DOCUMENT;
        }
        return TypeEnum.OTHER;
    }

    @Override
    public String toString() {
        return name;
    }
}

/**
 * 缩略图生成策略接口
 */
interface ThumbnailGeneratePolicy {
    void generate(String uuid);
}

/**
 * 视频缩略图生成策略
 */
class VideoGeneratePolicy implements ThumbnailGeneratePolicy {

    @Override
    public void generate(String uuid) {
        int frameNumber = new Random().nextInt(80000);
        File thumbnailFile = FileUtil.getFileThumbnailPath(uuid).toFile();
        //如果缩略图已经存在 则返回
        if (thumbnailFile.exists()) {
            return;
        }
        File tempFile = new File(Property.FRAME_TEMP_DIR + File.separator + uuid);
        if (!thumbnailFile.exists()) {
            thumbnailFile.getParentFile().mkdirs();
        }
        if (!tempFile.exists()) {
            tempFile.getParentFile().mkdirs();
        }
        try {
            Picture picture = FrameGrab.getFrameFromFile(FileUtil.getFilePath(uuid).toFile(), frameNumber);
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
}

/**
 * 图片缩略图生成策略
 */

class ImageGeneratePolicy implements ThumbnailGeneratePolicy {

    @Override
    public void generate(String uuid) {
        //创建路径

        File thumbnailFile = FileUtil.getFileThumbnailPath(uuid).toFile();
        //如果缩略图存在 则跳过
        if (thumbnailFile.exists()) {
            return;
        }

        if (!thumbnailFile.exists()) {
            thumbnailFile.getParentFile().mkdirs();
        }
        Thumbnails.Builder<BufferedImage> builder = null;
        try {
            BufferedImage image = ImageIO.read(FileUtil.getFilePath(uuid).toFile());
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
            builder.outputFormat("jpg").toFile(FileUtil.getFileThumbnailPath(uuid).toFile());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
