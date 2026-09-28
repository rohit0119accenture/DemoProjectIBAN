package com.demobank.transfer.controller;

import com.demobank.transfer.model.TransferRequest;
import com.demobank.transfer.model.TransferResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TransferController {

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> submitTransfer(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(new TransferResponse(
                "Transfer to " + request.recipientName() + " submitted successfully.",
                "SUCCESS"
        ));
    }
}
