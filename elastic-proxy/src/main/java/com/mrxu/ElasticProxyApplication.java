package com.mrxu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.tools.agent.ReactorDebugAgent;

@EnableScheduling
@SpringBootApplication
public class ElasticProxyApplication {
	public static void main(String[] args) {
//		DirectMemoryReporter.getInstance().startReport();
		ReactorDebugAgent.init();
		SpringApplication.run(ElasticProxyApplication.class, args);
	}
}
