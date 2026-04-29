package com.spring.graphql.exception;

/**
 * Ném ra khi user chưa đăng nhập (chưa có token / token invalid)
 * => GraphQL trả về error code: UNAUTHENTICATED
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}