package com.spring.graphql.resolver;

import com.spring.graphql.annotation.RequireLogin;
import com.spring.graphql.config.GraphQLContextInterceptor;
import com.spring.graphql.dto.AuthPayload;
import com.spring.graphql.dto.Inputs.*;
import com.spring.graphql.model.User;
import com.spring.graphql.service.AuthService;
import graphql.GraphQLContext;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthService authService;

    // PRIVATE - có @RequireLogin
    // PUBLIC — không có @RequireLogin

    // ============================================================================
    // Queries
    // ============================================================================
    @MutationMapping
    public AuthPayload login(@Argument LoginInput input) {
        return authService.login(input);
    }

    @MutationMapping
    public AuthPayload register(@Argument RegisterInput input) {
        return authService.register(input);
    }

    @MutationMapping
    public AuthPayload refreshToken(@Argument RefreshTokenInput input) {
        return authService.refreshToken(input.refreshToken());
    }

    // ============================================================================
    // Mutations
    // ============================================================================
    @MutationMapping
    @RequireLogin
    public boolean logout(GraphQLContext ctx) {
        User user = ctx.get(GraphQLContextInterceptor.CURRENT_USER_KEY);
        return authService.logout(user.getUsername());
    }
}
