package com.radhika.payflow.auth.service;

import com.radhika.payflow.auth.dto.AuthResponse;
import com.radhika.payflow.auth.dto.LoginRequest;
import com.radhika.payflow.auth.dto.RegisterRequest;
import com.radhika.payflow.auth.entity.User;
import com.radhika.payflow.common.exception.EmailAlreadyExistsException;
import com.radhika.payflow.common.exception.InvalidCredentialsException;
import com.radhika.payflow.common.repository.UserRepository;
import com.radhika.payflow.security.JwtUtil;
import com.radhika.payflow.wallet.entity.Account;
import com.radhika.payflow.wallet.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
@Transactional
public AuthResponse register(RegisterRequest request){

    if(userRepository.findByEmail(request.getEmail()).isPresent()){
        throw new EmailAlreadyExistsException("Email already registered:"+ request.getEmail());
    }
User user =User.builder()
        .fullName(request.getFullName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .build();
User savedUser=userRepository.save(user);

    Account account = Account.builder()
            .user(savedUser)
            .balance(BigDecimal.ZERO)
            .build();
    accountRepository.save(account);

    String token = jwtUtil.generateToken(savedUser.getEmail());

    return AuthResponse.builder()
            .token(token)
            .email(savedUser.getEmail())
            .fullName(savedUser.getFullName())
            .build();

}

public AuthResponse login(LoginRequest request){
    User user=userRepository.findByEmail(request.getEmail())
            .orElseThrow(()->new InvalidCredentialsException("Invalid email or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new InvalidCredentialsException("Invalid email or password");
    }


    String token = jwtUtil.generateToken(user.getEmail());

    return AuthResponse.builder()
            .token(token)
            .email(user.getEmail())
            .fullName(user.getFullName())
            .build();
}



}
