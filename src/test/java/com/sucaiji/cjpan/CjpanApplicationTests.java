package com.sucaiji.cjpan;


import com.sucaiji.cjpan.config.Property;
import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.dao.Md5Dao;
import com.sucaiji.cjpan.dao.UserDao;
import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.entity.Page;

import com.sucaiji.cjpan.service.IndexService;

import com.sucaiji.cjpan.util.Md5Util;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.boxes.MetaValue;
import org.jcodec.movtool.MetadataEditor;
import org.jcodec.scale.AWTUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import static com.sucaiji.cjpan.config.Property.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class CjpanApplicationTests {


	final static Logger logger=LoggerFactory.getLogger(CjpanApplicationTests.class);

	@Autowired
	private IndexService indexService;

	@Autowired
	private IndexDao indexDao;

	@Test
	public void test() {

	}
	private Path basePath;
	private Path dataPath;
	private Path tempPath;
	@Test
	public void refactorSave() {
		basePath = Paths.get(System.getProperty("user.dir") + File.separator + APP_NAME_EN);
		dataPath = Paths.get(basePath.toString() + File.separator + DATA_DIR);
		tempPath = Paths.get(basePath.toString() + File.separator + TEMP_DIR);
		try {

			String md5 = UUID();
			indexService.saveTemp(new MockMultipartFile("testName", "qwer".getBytes()), md5, 1);
			indexService.saveTemp(new MockMultipartFile("testName", "qwer".getBytes()), md5, 2);
			indexService.saveTemp(new MockMultipartFile("testName", "qwer".getBytes()), md5, 3);
			indexService.saveTemp(new MockMultipartFile("testName", "qwer".getBytes()), md5, 4);
			indexService.saveTemp(new MockMultipartFile("testName", "qwer".getBytes()), md5, 5);
			indexService.saveFile(Property.ROOT, md5, "名杂", 5);


			String realMd5 = Md5Util.getMD5(dataPath.toString() + File.separator + md5.substring(0, 4) + File.separator + md5);
			//检测文件md5是否正确
			assert "fb363f5e81efddce810df89cdb6ea19d".equalsIgnoreCase(realMd5);

			//查询文件是否正确保存到数据库
			Map<String, Object> map = new HashMap<>();
			map.put("uuid", md5);
			List<Index> indexs = indexDao.selectIndex(map);
			assert indexs.get(0).getSize().equals(20L);
			assert indexs.get(0).getUuid().equals(md5);
			assert indexs.get(0).getName().equals("名杂");

			//判断temp文件夹是否被干净删除
			File file = new File(tempPath.toString() + File.separator + md5 + File.separator + md5);
			assert !file.getParentFile().exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	private String UUID() {
		return java.util.UUID.randomUUID().toString().replaceAll("-", "");
	}

}
