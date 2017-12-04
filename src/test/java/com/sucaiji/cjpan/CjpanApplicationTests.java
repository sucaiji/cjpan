package com.sucaiji.cjpan;


import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.dao.Md5Dao;
import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.service.IndexService;
import net.coobird.thumbnailator.Thumbnailator;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RunWith(SpringRunner.class)
@SpringBootTest
public class CjpanApplicationTests {
	@Autowired
	private Md5Dao md5Dao;
	@Autowired
	private IndexDao indexDao;
	@Autowired
	private IndexService indexService;
	@Test
	public void contextLoads() {
		Thumbnails.Builder<BufferedImage> builder = null;
		try {
			BufferedImage image = ImageIO.read(new File("C:\\Users\\sufchick\\Desktop\\IMG_1897.JPG"));
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
			builder.outputFormat("jpg").toFile("E:\\1");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}



}
