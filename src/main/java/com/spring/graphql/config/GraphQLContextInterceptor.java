package com.spring.graphql.config;

import com.spring.graphql.model.User;
import com.spring.graphql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Chạy với MỖI GraphQL request — sau JwtAuthFilter.
 *
 * Nhiệm vụ:
 *   Đọc Authentication từ SecurityContext (JwtAuthFilter đã set)
 *   => Load User từ PostgreSQL
 *   => Đặt vào GraphQL context với key CURRENT_USER_KEY
 *
 * Nhờ đó @RequireLogin Aspect có thể lấy user ra kiểm tra, và resolver lấy user hiện tại mà không cần query DB thêm lần nữa.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GraphQLContextInterceptor implements WebGraphQlInterceptor {

    public static final String CURRENT_USER_KEY = "CURRENT_USER";

    private final UserRepository userRepository;

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {

        Authentication authentication = SecurityContextHolder
                .getContext().getAuthentication();

        User currentUser = null;

        if (isAuthenticated(authentication)) {
            // Load User entity từ PostgreSQL theo username
            currentUser = userRepository
                    .findByUsername(authentication.getName())
                    .orElse(null);

            if (currentUser != null) {
                log.debug("[GraphQLContext] User: {} | Operation: {}",
                        currentUser.getUsername(), request.getOperationName());
            }
        }

        // Inject vào GraphQL context (null nếu chưa login)
        final User userForCtx = currentUser;
        request.configureExecutionInput((executionInput, builder) ->
                builder.graphQLContext(ctx ->
                        ctx.put(CURRENT_USER_KEY, userForCtx != null ? userForCtx : "ANONYMOUS")
//                        ctx.put(CURRENT_USER_KEY, userForCtx)
                ).build()
        );

        return chain.next(request);
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }
}