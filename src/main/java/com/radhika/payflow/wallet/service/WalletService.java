package com.radhika.payflow.wallet.service;

import com.radhika.payflow.auth.entity.User;
import com.radhika.payflow.common.exception.DuplicateRequestException;
import com.radhika.payflow.common.exception.ResourceNotFoundException;
import com.radhika.payflow.common.repository.UserRepository;
import com.radhika.payflow.security.SecurityUtil;
import com.radhika.payflow.transaction.entity.Transaction;
import com.radhika.payflow.transaction.enums.TransactionStatus;
import com.radhika.payflow.transaction.enums.TransactionType;
import com.radhika.payflow.transaction.repository.TransactionRepository;
import com.radhika.payflow.wallet.dto.BalanceResponse;
import com.radhika.payflow.wallet.entity.Account;
import com.radhika.payflow.wallet.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityUtil securityUtil;


public BalanceResponse getBalance(){
    Account account = getAccountOfCurrentUser();
    return BalanceResponse.builder()
            .balance(account.getBalance())
            .build();
}
@Transactional(isolation= Isolation.SERIALIZABLE)
public BalanceResponse addMoney(BigDecimal amount, String idempotencyKey){
if(transactionRepository.findByIdempotencyKey(idempotencyKey).isPresent()){
    throw new DuplicateRequestException("this request has already been processed");
}
 Account account =getAccountOfCurrentUser();
Account lockedAccount = accountRepository.findByIdForUpdate(account.getId())
        .orElseThrow(()->new ResourceNotFoundException("account not found"));
 lockedAccount.setBalance(lockedAccount.getBalance().add(amount));
 accountRepository.save(lockedAccount);


 Transaction transaction = Transaction.builder()
         .senderAccount(null)
         .receiverAccount(lockedAccount)
         .amount(amount)
         .type(TransactionType.ADD_MONEY)
         .status(TransactionStatus.SUCCESS)
         .idempotencyKey(idempotencyKey)
         .build();
    transactionRepository.save(transaction);

    return BalanceResponse.builder()
            .balance(lockedAccount.getBalance())
            .build();
}

    private Account getAccountOfCurrentUser() {
        String email = securityUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return accountRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for this user"));
    }
}
