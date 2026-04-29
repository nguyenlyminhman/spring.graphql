package com.spring.graphql.aspect;

import com.spring.graphql.annotation.RequireLogin;
import com.spring.graphql.config.GraphQLContextInterceptor;
import com.spring.graphql.exception.UnauthorizedException;
import com.spring.graphql.model.User;
import graphql.GraphQLContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP Aspect xử lý @RequireLogin.
 *
 * Luồng:
 *   Client gọi resolver
 *       ↓
 *   Spring AOP thấy @RequireLogin → gọi checkLogin() ở đây
 *       ↓ (pass)              ↓ (fail)
 *   resolver chạy        throw UnauthorizedException
 *                              ↓
 *                        GraphQLExceptionHandler
 *                              ↓
 *                        { "code": "UNAUTHENTICATED" }
 */
@Aspect
@Component
@Slf4j
public class RequireLoginAspect {

    /**
     * Chạy TRƯỚC (@Before) bất kỳ method nào có @RequireLogin.
     *
     * @annotation(requireLogin) làm 2 việc:
     *   1. Chỉ match method có @RequireLogin
     *   2. Inject annotation instance vào tham số (dùng sau nếu cần đọc attributes)
     */
    @Before("@annotation(requireLogin)")
    public void checkLogin(JoinPoint joinPoint, RequireLogin requireLogin) {

        // Tìm GraphQLContext trong danh sách tham số của method
        GraphQLContext ctx = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg instanceof GraphQLContext)
                .map(arg -> (GraphQLContext) arg)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "@RequireLogin yêu cầu method phải có tham số GraphQLContext. "
                                + "Method bị lỗi: " + joinPoint.getSignature().toShortString()
                ));

        // Lấy user đã được GraphQLContextInterceptor inject vào context
        User user = ctx.get(GraphQLContextInterceptor.CURRENT_USER_KEY);

        if (user == null) {
            log.warn("[RequireLogin] Truy cập trái phép vào: {}",
                    joinPoint.getSignature().toShortString());
            throw new UnauthorizedException(
                    "Bạn cần đăng nhập để thực hiện thao tác này");
        }

        log.debug("[RequireLogin] OK - user: {} → {}",
                user.getUsername(),
                joinPoint.getSignature().getName());
    }
}
