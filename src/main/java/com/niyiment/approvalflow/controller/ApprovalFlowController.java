package com.niyiment.approvalflow.controller;


import com.niyiment.approvalflow.service.FileTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class ApprovalFlowController {
    private final FileTrackingService service;

    @GetMapping("/vendor-payment")
    public void vendorPaymentApprovalDemo() {
        service.vendorPaymentApprovalDemo();
    }

    @GetMapping("/leave-request")
    public void leaveApprovalDemo(){
        service.leaveApprovalDemo();
    }
}
