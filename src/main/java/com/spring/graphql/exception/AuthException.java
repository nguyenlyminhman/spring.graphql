package com.spring.graphql.exception;

/**
 * Ném ra khi có lỗi nghiệp vụ liên quan đến auth
 * Ví dụ: sai mật khẩu, username đã tồn tại, refresh token hết hạn
 * => GraphQL trả về error code: AUTH_ERROR
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}