package com.sucaiji.cjpan;


import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.dao.Md5Dao;
import com.sucaiji.cjpan.dao.UserDao;
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
	private UserDao userDao;
	@Autowired
	private IndexDao indexDao;
	@Autowired
	private IndexService indexService;
	@Test
	public void contextLoads() {
		String rangeStr="0-4310393";
		Long range;
		Pattern pattern3 = Pattern.compile("\\d+-\\d+");
		Matcher matcher3 = pattern3.matcher(rangeStr);
		if (matcher3.matches()) {
			System.out.println(rangeStr);
			rangeStr=rangeStr.replaceAll("-\\d+$","");
			System.out.println(rangeStr);
			range = Long.valueOf(rangeStr);
		}
	}



}
