package com.radhika.payflow.transaction.service;

import com.radhika.payflow.auth.entity.User;
import com.radhika.payflow.common.exception.DuplicateRequestException;
import com.radhika.payflow.common.repository.UserRepository;
import com.radhika.payflow.security.SecurityUtil;
import com.radhika.payflow.transaction.repository.TransactionRepository;
import com.radhika.payflow.wallet.dto.BalanceResponse;
import com.radhika.payflow.wallet.entity.Account;
import com.radhika.payflow.wallet.repository.AccountRepository;
import com.radhika.payflow.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private WalletService walletService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("radhika@example.com")
                .build();

        account = Account.builder()
                .id(UUID.randomUUID())
                .user(user)
                .balance(new BigDecimal("1000"))
                .build();
    }

    @Test
    void getBalance_cacheMiss_fetchesFromDbAndCaches() {

        when(securityUtil.getCurrentUserEmail()).thenReturn("radhika@example.com");
        when(userRepository.findByEmail("radhika@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(user.getId())).thenReturn(Optional.of(account));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);   // Cache MISS — Redis mein kuch nahi mila

        BalanceResponse response = walletService.getBalance();

        assertEquals(new BigDecimal("1000"), response.getBalance());

        verify(valueOperations, times(1)).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    void addMoney_success_updatesBalanceAndInvalidatesCache() {

        when(transactionRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        when(securityUtil.getCurrentUserEmail()).thenReturn("radhika@example.com");
        when(userRepository.findByEmail("radhika@example.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByUserId(user.getId())).thenReturn(Optional.of(account));
        when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));

        BalanceResponse response = walletService.addMoney(new BigDecimal("500"), "add-money-key-001");

        assertEquals(new BigDecimal("1500"), response.getBalance());   // 1000 + 500

        verify(accountRepository, times(1)).save(any(Account.class));
        verify(redisTemplate, times(1)).delete(anyString());   // cache invalidate hui — DELETE call hui
    }

     @Test
    void addMoney_duplicateIdempotencyKey_throwsException() {

        when(transactionRepository.findByIdempotencyKey("duplicate-key"))
                .thenReturn(Optional.of(mock(com.radhika.payflow.transaction.entity.Transaction.class)));

        assertThrows(DuplicateRequestException.class, () ->
                walletService.addMoney(new BigDecimal("500"), "duplicate-key")
        );

        verify(accountRepository, never()).save(any(Account.class));
        verify(redisTemplate, never()).delete(anyString());
    }
}