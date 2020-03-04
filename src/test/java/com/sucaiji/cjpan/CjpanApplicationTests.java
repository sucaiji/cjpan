package com.sucaiji.cjpan;


import com.sucaiji.cjpan.config.Property;
import com.sucaiji.cjpan.dao.IndexDao;
import com.sucaiji.cjpan.model.Index;

import com.sucaiji.cjpan.service.IndexService;

import com.sucaiji.cjpan.util.Md5Util;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


import java.io.File;

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
		basePath = Paths.get(System.getProperty("user.dir") + File.separator + APP_DIR);
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
			Index queryIndex = new Index();
			queryIndex.setUuid(md5);
			List<Index> indexs = indexDao.selectIndex(queryIndex);
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
