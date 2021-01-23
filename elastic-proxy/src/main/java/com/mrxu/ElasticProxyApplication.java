package com.mrxu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ElasticProxyApplication {
	public static void main(String[] args) {
//		DirectMemoryReporter.getInstance().startReport();
		SpringApplication.run(ElasticProxyApplication.class, args);
	}
}
