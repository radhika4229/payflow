package com.radhika.payflow.transaction.controller;

import com.radhika.payflow.transaction.dto.TransferRequest;
import com.radhika.payflow.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@Valid @RequestBody TransferRequest request) {
        transactionService.transfer(request.getReceiverAccountId(), request.getAmount(), request.getIdempotencyKey());
        return ResponseEntity.ok("Transfer successful");
    }
}
