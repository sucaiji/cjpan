package com.sucaiji.cjpan;


import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.dao.Md5Dao;
import com.sucaiji.cjpan.entity.Index;
import com.sucaiji.cjpan.service.IndexService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
		String suffix=".txt".replaceAll("\\.","");
		//回头扩充一下
		Pattern videoPattern = Pattern.compile("(mp4|rm|rmvb|wmv|avi|3gp|mkv)");
		Pattern imagePattern = Pattern.compile("(mp3|wav|wma)");
		Pattern docPattern = Pattern.compile("txt|pdf");
		Matcher videoMatcher = videoPattern.matcher(suffix);
		if(videoMatcher.matches()){
			System.out.println("是视频");
			return;
		}
		Matcher imageMatcher = imagePattern.matcher(suffix);
		if(imageMatcher.matches()){
			System.out.println("是图片");
			return;
		}
		Matcher docMatcher = docPattern.matcher(suffix);
		if(docMatcher.matches()){
			System.out.println("是文档");
			return;
		}




	}



}
