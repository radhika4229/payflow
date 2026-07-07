package com.radhika.payflow.wallet.controller;

import com.radhika.payflow.wallet.dto.AddMoneyRequest;
import com.radhika.payflow.wallet.dto.BalanceResponse;
import com.radhika.payflow.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance() {
        return ResponseEntity.ok(walletService.getBalance());
    }
    @PostMapping("/add-money")
    public ResponseEntity<BalanceResponse> addMoney(@Valid @RequestBody AddMoneyRequest request) {
        BalanceResponse response = walletService.addMoney(request.getAmount(), request.getIdempotencyKey());
        return ResponseEntity.ok(response);
    }
}
