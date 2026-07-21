package com.radhika.payflow.transaction.service;

import com.radhika.payflow.auth.dto.AuthResponse;
import com.radhika.payflow.auth.dto.RegisterRequest;
import com.radhika.payflow.auth.entity.User;
import com.radhika.payflow.auth.service.AuthService;
import com.radhika.payflow.common.exception.EmailAlreadyExistsException;
import com.radhika.payflow.common.repository.UserRepository;
import com.radhika.payflow.security.JwtUtil;
import com.radhika.payflow.wallet.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
   private UserRepository userRepository;
    @Mock
   private AccountRepository accountRepository;
    @Mock
     private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil  jwtUtil;
    @InjectMocks
    private AuthService authService;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Radhika");
        registerRequest.setEmail("radhika@example.com");
        registerRequest.setPassword("password123");
    }
    @Test
    void register_success_createsUserAndAccount() {

        when(userRepository.findByEmail("radhika@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password-xyz");
        User savedUser = User.builder()
                .id(java.util.UUID.randomUUID())
                .fullName("Radhika Sharma")
                .email("radhika@example.com")
                .password("hashed-password-xyz")
                .build();

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);
        when(jwtUtil.generateToken("radhika@example.com"))
                .thenReturn("fake-jwt-token");
        AuthResponse response = authService.register(registerRequest);

        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("radhika@example.com", response.getEmail());
        assertEquals("Radhika Sharma", response.getFullName());

        verify(userRepository, times(1)).save(any(User.class));
        verify(accountRepository, times(1)).save(any());
    }
    @Test
    void register_duplicateEmail_throwsException() {
  User existingUser = User.builder()
                .id(java.util.UUID.randomUUID())
                .email("radhika@example.com")
                .build();

        when(userRepository.findByEmail("radhika@example.com"))
                .thenReturn(Optional.of(existingUser));

    assertThrows(EmailAlreadyExistsException.class, () ->
                authService.register(registerRequest)
        );
   verify(userRepository, never()).save(any(User.class));
        verify(accountRepository, never()).save(any());
    }

}
