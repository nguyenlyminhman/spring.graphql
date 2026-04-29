package com.spring.graphql.service;

import com.spring.graphql.dto.AuthPayload;
import com.spring.graphql.dto.Inputs.*;
import com.spring.graphql.exception.AuthException;
import com.spring.graphql.model.RefreshToken;
import com.spring.graphql.model.User;
import com.spring.graphql.repository.RefreshTokenRepository;
import com.spring.graphql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository          userRepository;
    private final RefreshTokenRepository  refreshTokenRepository;
    private final JwtService              jwtService;
    private final PasswordEncoder         passwordEncoder;
    private final AuthenticationManager   authenticationManager;

    @Value("${app.jwt.refresh-expiration-seconds}")
    private long refreshExpirationSeconds;

    // Login ===========================================
    public AuthPayload login(LoginInput input) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.username(), input.password()));

            User user = (User) authentication.getPrincipal();
            log.info("User đăng nhập thành công: {}", user.getUsername());
            return buildAuthPayload(user);

        } catch (BadCredentialsException e) {
            throw new AuthException("Sai username hoặc password");
        } catch (DisabledException e) {
            throw new AuthException("Tài khoản đã bị vô hiệu hóa");
        }
    }

    // Register ===========================================
    public AuthPayload register(RegisterInput input) {
        if (userRepository.existsByUsername(input.username())) {
            throw new AuthException("Username '" + input.username() + "' đã tồn tại");
        }
        if (userRepository.existsByEmail(input.email())) {
            throw new AuthException("Email '" + input.email() + "' đã được đăng ký");
        }

        User user = User.builder()
                .username(input.username())
                .email(input.email())
                .password(passwordEncoder.encode(input.password()))
                .build();

        user = userRepository.save(user);
        log.info("User mới đăng ký: {}", user.getUsername());
        return buildAuthPayload(user);
    }

    // Refresh Token ===========================================
    public AuthPayload refreshToken(String refreshTokenStr) {
        RefreshToken stored = refreshTokenRepository
                .findByToken(refreshTokenStr)
                .orElseThrow(() -> new AuthException("Refresh token không hợp lệ"));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new AuthException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        User user = stored.getUser();

        // Rotate: xóa token cũ → tạo token mới (bảo mật hơn)
        refreshTokenRepository.delete(stored);

        log.info("Token refreshed cho user: {}", user.getUsername());
        return buildAuthPayload(user);
    }

    // Logout ===========================================
    public boolean logout(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            refreshTokenRepository.deleteAllByUser(user);
            log.info("User đăng xuất: {}", username);
        });
        return true;
    }

    // Private helpers ===========================================
    private AuthPayload buildAuthPayload(User user) {
        String       accessToken  = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        return new AuthPayload(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                (int) (jwtService.getExpirationMs() / 1000),
                user
        );
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusSeconds(refreshExpirationSeconds))
                .build();
        return refreshTokenRepository.save(token);
    }
}
