package com.niyiment.approvalflow;

import com.niyiment.approvalflow.enums.Action;
import com.niyiment.approvalflow.enums.FileType;
import com.niyiment.approvalflow.models.FileTrackingItem;
import com.niyiment.approvalflow.models.StateTransition;
import com.niyiment.approvalflow.service.FileTrackingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApprovalFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApprovalFlowApplication.class, args);
	}


}
