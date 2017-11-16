package com.sucaiji.cjpan;

import com.sucaiji.cjpan.service.Md5Service;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.sucaiji.cjpan.file.FileOperate.APP_NAME_EN;
import static com.sucaiji.cjpan.file.FileOperate.DATA_DIR;
import static com.sucaiji.cjpan.file.FileOperate.TEMP_DIR;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CjpanApplicationTests {
	@Autowired
	private Md5Service md5Service;

	private Path basePath;
	private Path dataPath;
	private Path tempPath;






	@Test
	public void contextLoads() {
		basePath= Paths.get(System.getProperty("user.dir")+File.separator+APP_NAME_EN);
		dataPath=Paths.get(basePath.toString()+File.separator+DATA_DIR);
		tempPath=Paths.get(basePath.toString()+File.separator+TEMP_DIR);

		String fileMd5="123123123qwe";
		int total=17;



	}

}
