package com.spring.graphql.users;

import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserResolver {

    private final UserRepository userRepository;

    // Chỉ USER trở lên mới xem được profile của mình
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public User me(Authentication authentication) {
        String username = authentication.getName();
        return null;
        // return userRepository.findByUsername(username).orElseThrow(() -> new GraphQLException("User not found"));
    }

    // Chỉ ADMIN mới xem được tất cả users
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> allUsers() {
        return new ArrayList<>();
    }

    // Kết hợp nhiều điều kiện
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public User banUser(@Argument Long userId) {
        return null;
    }
}
