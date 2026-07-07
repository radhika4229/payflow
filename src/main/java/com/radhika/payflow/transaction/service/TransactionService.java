package com.radhika.payflow.transaction.service;
import com.radhika.payflow.auth.entity.User;
import com.radhika.payflow.common.exception.DuplicateRequestException;
import com.radhika.payflow.common.exception.InsufficientBalanceException;
import com.radhika.payflow.common.exception.InvalidTransferException;
import com.radhika.payflow.common.exception.ResourceNotFoundException;
import com.radhika.payflow.common.repository.UserRepository;
import com.radhika.payflow.security.SecurityUtil;
import com.radhika.payflow.transaction.entity.Transaction;
import com.radhika.payflow.transaction.enums.TransactionStatus;
import com.radhika.payflow.transaction.enums.TransactionType;
import com.radhika.payflow.transaction.repository.TransactionRepository;
import com.radhika.payflow.wallet.entity.Account;
import com.radhika.payflow.wallet.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SecurityUtil securityUtil;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void transfer(UUID receiverAccountId, BigDecimal amount, String idempotencyKey) {


        if (transactionRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new DuplicateRequestException("This request has already been processed");
        }

          String email = securityUtil.getCurrentUserEmail();
        User senderUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account senderAccountRaw = accountRepository.findByUserId(senderUser.getId())
                .orElseThrow();

        if (senderAccountRaw.getId().equals(receiverAccountId)) {
            throw new InvalidTransferException("Cannot transfer money to your own account");
        }

          UUID firstLockId = senderAccountRaw.getId().compareTo(receiverAccountId) < 0
                ? senderAccountRaw.getId() : receiverAccountId;
        UUID secondLockId = senderAccountRaw.getId().compareTo(receiverAccountId) < 0
                ? receiverAccountId : senderAccountRaw.getId();

        Account firstLocked = accountRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Account secondLocked = accountRepository.findByIdForUpdate(secondLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

            Account sender = firstLocked.getId().equals(senderAccountRaw.getId()) ? firstLocked : secondLocked;
        Account receiver = firstLocked.getId().equals(receiverAccountId) ? firstLocked : secondLocked;
  if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

          sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

            Transaction transaction = Transaction.builder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .idempotencyKey(idempotencyKey)
                .build();
        transactionRepository.save(transaction);
    }
}
