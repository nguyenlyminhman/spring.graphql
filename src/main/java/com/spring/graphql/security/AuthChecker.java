package com.spring.graphql.security;

import com.spring.graphql.config.GraphQLContextInterceptor;
import com.spring.graphql.exception.UnauthorizedException;
import com.spring.graphql.model.User;
import graphql.GraphQLContext;
import org.springframework.stereotype.Component;

/**
 * Helper dùng lại trong mọi Resolver cần authentication.
 *
 * Thay vì viết lại logic check ở từng resolver,
 * chỉ cần gọi: User user = authChecker.requireLogin(ctx);
 */
@Component
public class AuthChecker {

    /**
     * Kiểm tra user đã đăng nhập chưa.
     *
     * @return User hiện tại nếu đã đăng nhập
     * @throws UnauthorizedException nếu chưa đăng nhập
     */
    public User requireLogin(GraphQLContext ctx) {
        User user = ctx.get(GraphQLContextInterceptor.CURRENT_USER_KEY);

        if (user == null) {
            throw new UnauthorizedException(
                    "Bạn cần đăng nhập để thực hiện thao tác này");
        }

        return user;
    }

    /**
     * Lấy user hiện tại, trả về null nếu chưa đăng nhập.
     * Dùng cho các operation public nhưng muốn biết user là ai.
     */
    public User getCurrentUser(GraphQLContext ctx) {
        return ctx.get(GraphQLContextInterceptor.CURRENT_USER_KEY);
    }

    /**
     * Kiểm tra đã đăng nhập chưa (không throw exception).
     */
    public boolean isLoggedIn(GraphQLContext ctx) {
        User user = ctx.get(GraphQLContextInterceptor.CURRENT_USER_KEY);
        return user != null;
    }
}
