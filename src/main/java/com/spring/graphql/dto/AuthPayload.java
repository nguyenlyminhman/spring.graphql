package com.spring.graphql.dto;

import com.spring.graphql.model.User;

public record AuthPayload(
        String accessToken,
        String refreshToken,
        String tokenType,
        int    expiresIn,   // seconds
        User   user
) {}
