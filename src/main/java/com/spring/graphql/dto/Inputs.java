package com.spring.graphql.dto;

public class Inputs {
    public record LoginInput(String username, String password) {}

    public record RegisterInput(String username, String email, String password) {}

    public record CreatePostInput(String title, String content) {}

    public record RefreshTokenInput(String refreshToken) {}
}
