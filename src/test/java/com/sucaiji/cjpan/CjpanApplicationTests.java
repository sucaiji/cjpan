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

		List<String> list1=md5Dao.selectUuidByMd5("asfasg");
		if(list1==null){
			System.out.println("123123");
		}
		System.out.println(list1.size());
	}



}
