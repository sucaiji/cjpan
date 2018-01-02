package com.sucaiji.cjpan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class CjpanApplication {
	public static void main(String[] args) {
		SpringApplication.run(CjpanApplication.class, args);
	}
}
