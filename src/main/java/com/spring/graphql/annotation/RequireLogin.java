package com.spring.graphql.annotation;

import java.lang.annotation.*;

/**
 * Đặt annotation này lên GraphQL resolver method để yêu cầu user đã đăng nhập.
 * <p>
 * Cách hoạt động (AOP):
 * 1. Spring AOP chặn method có @RequireLogin TRƯỚC khi chạy
 * 2. RequireLoginAspect kiểm tra GraphQLContext có CURRENT_USER không
 * 3. Không có  → throw UnauthorizedException → GraphQL trả lỗi UNAUTHENTICATED
 * 4. Có        → method chạy bình thường
 * <p>
 * Yêu cầu: method phải khai báo tham số GraphQLContext.
 * <p>
 * Ví dụ sử dụng:
 * <pre>
 *   {@literal @}QueryMapping
 *   {@literal @}RequireLogin
 *   public User me(GraphQLContext ctx) {
 *       // Vào đây = chắc chắn đã login
 *       return ctx.get(CURRENT_USER_KEY);
 *   }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}
