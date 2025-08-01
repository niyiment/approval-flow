package com.niyiment.approvalflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class ApprovalFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApprovalFlowApplication.class, args);
	}

}
