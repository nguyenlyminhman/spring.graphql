package com.spring.graphql.auth;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;

@AllArgsConstructor
public class AuthPayload {
    private String token;
    private User user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
