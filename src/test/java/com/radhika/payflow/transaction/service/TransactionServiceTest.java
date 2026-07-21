package com.radhika.payflow.transaction.service;

import com.radhika.payflow.auth.entity.User;
import com.radhika.payflow.common.exception.*;
import com.radhika.payflow.common.repository.UserRepository;
import com.radhika.payflow.security.SecurityUtil;
import com.radhika.payflow.transaction.event.TransactionEventProducer;
import com.radhika.payflow.transaction.repository.TransactionRepository;
import com.radhika.payflow.wallet.entity.Account;
import com.radhika.payflow.wallet.repository.AccountRepository;
import com.radhika.payflow.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private WalletService walletService;

    @Mock
    private TransactionEventProducer eventProducer;

    @InjectMocks
    private TransactionService transactionService;

     private User senderUser;
    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        senderUser = User.builder()
                .id(UUID.randomUUID())
                .email("radhika@example.com")
                .build();

        senderAccount = Account.builder()
                .id(UUID.randomUUID())
                .user(senderUser)
                .balance(new BigDecimal("1000"))
                .build();

        receiverAccount = Account.builder()
                .id(UUID.randomUUID())
                .balance(new BigDecimal("500"))
                .build();
    }


    @Test
    void transfer_success_updatesBalances() {
            when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(securityUtil.getCurrentUserEmail()).thenReturn("radhika@example.com");
        when(userRepository.findByEmail("radhika@example.com")).thenReturn(Optional.of(senderUser));
        when(accountRepository.findByUserId(senderUser.getId())).thenReturn(Optional.of(senderAccount));

            when(accountRepository.findByIdForUpdate(senderAccount.getId())).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIdForUpdate(receiverAccount.getId())).thenReturn(Optional.of(receiverAccount));

            transactionService.transfer(receiverAccount.getId(), new BigDecimal("300"), "test-key-001");

         assertEquals(new BigDecimal("700"), senderAccount.getBalance());    // 1000 - 300
        assertEquals(new BigDecimal("800"), receiverAccount.getBalance());  // 500 + 300

          verify(accountRepository, times(2)).save(any(Account.class));
        verify(eventProducer, times(1)).publishEvent(any());
    }

     @Test
    void transfer_insufficientBalance_throwsException() {
        when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(securityUtil.getCurrentUserEmail()).thenReturn("radhika@example.com");
        when(userRepository.findByEmail("radhika@example.com")).thenReturn(Optional.of(senderUser));
        when(accountRepository.findByUserId(senderUser.getId())).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIdForUpdate(senderAccount.getId())).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIdForUpdate(receiverAccount.getId())).thenReturn(Optional.of(receiverAccount));

       assertThrows(InsufficientBalanceException.class, () ->
                transactionService.transfer(receiverAccount.getId(), new BigDecimal("5000"), "test-key-002")
        );

           verify(accountRepository, never()).save(any(Account.class));
    }

        @Test
    void transfer_selfTransfer_throwsException() {
        when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(securityUtil.getCurrentUserEmail()).thenReturn("radhika@example.com");
        when(userRepository.findByEmail("radhika@example.com")).thenReturn(Optional.of(senderUser));
        when(accountRepository.findByUserId(senderUser.getId())).thenReturn(Optional.of(senderAccount));

          assertThrows(InvalidTransferException.class, () ->
                transactionService.transfer(senderAccount.getId(), new BigDecimal("100"), "test-key-003")
        );

        verify(accountRepository, never()).save(any(Account.class));
    }
   @Test
    void transfer_duplicateIdempotencyKey_throwsException() {
           when(transactionRepository.findByIdempotencyKey("duplicate-key"))
                .thenReturn(Optional.of(mock(com.radhika.payflow.transaction.entity.Transaction.class)));

        assertThrows(DuplicateRequestException.class, () ->
                transactionService.transfer(receiverAccount.getId(), new BigDecimal("100"), "duplicate-key")
        );

           verify(accountRepository, never()).save(any(Account.class));
        verify(eventProducer, never()).publishEvent(any());
    }

      @Test
    void transfer_receiverNotFound_throwsException() {
        UUID fakeReceiverId = UUID.randomUUID();

        when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(securityUtil.getCurrentUserEmail()).thenReturn("radhika@example.com");
        when(userRepository.findByEmail("radhika@example.com")).thenReturn(Optional.of(senderUser));
        when(accountRepository.findByUserId(senderUser.getId())).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByIdForUpdate(any())).thenReturn(Optional.empty());   // account nahi mila

        assertThrows(ResourceNotFoundException.class, () ->
                transactionService.transfer(fakeReceiverId, new BigDecimal("100"), "test-key-005")
        );
    }
}