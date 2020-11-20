package com.mrxu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ElasticProxyApplication {
	public static void main(String[] args) {
		//打印对外内存占用情况，需要时打开
//		DirectMemoryReporter.getInstance().startReport();
		SpringApplication.run(ElasticProxyApplication.class, args);
	}
}
