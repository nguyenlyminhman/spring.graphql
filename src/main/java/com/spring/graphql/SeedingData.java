package com.spring.graphql;

import com.spring.graphql.model.User;
import com.spring.graphql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedingData implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // Chỉ tạo nếu chưa có (tránh lỗi unique constraint khi restart)
        if (userRepository.existsByUsername("alice")) {
            log.info("Test users đã tồn tại, bỏ qua seed data.");
            return;
        }

        User alice = User.builder()
                .username("alice")
                .email("alice@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();

        User bob = User.builder()
                .username("bob")
                .email("bob@example.com")
                .password(passwordEncoder.encode("password123"))
                .build();

        userRepository.save(alice);
        userRepository.save(bob);

        log.info("============================================");
        log.info("Test users đã được tạo trong PostgreSQL:");
        log.info("  username: alice  |  password: password123");
        log.info("  username: bob    |  password: password123");
        log.info("GraphiQL UI: http://localhost:8080/graphiql");
        log.info("============================================");
    }
}