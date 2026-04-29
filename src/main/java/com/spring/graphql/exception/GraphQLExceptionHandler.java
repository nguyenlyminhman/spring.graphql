package com.spring.graphql.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Map Java exceptions → GraphQL error format.
 *
 * GraphQL luôn trả về HTTP 200, lỗi nằm trong body:
 * {
 *   "errors": [{
 *     "message": "...",
 *     "extensions": { "code": "UNAUTHENTICATED" }
 *   }]
 * }
 */

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex,
                                                DataFetchingEnvironment env) {
        // Chưa đăng nhập / token invalid
        if (ex instanceof UnauthorizedException) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.UNAUTHORIZED)
                    .message(ex.getMessage())
                    .extensions(Map.of(
                            "code",      "UNAUTHENTICATED",
                            "timestamp", Instant.now().toString()
                    ))
                    .build();
        }

        // Lỗi nghiệp vụ auth (sai password, token hết hạn, ...)
        if (ex instanceof AuthException) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.getMessage())
                    .extensions(Map.of(
                            "code",      "AUTH_ERROR",
                            "timestamp", Instant.now().toString()
                    ))
                    .build();
        }

        return null; // Để Spring GraphQL xử lý mặc định
    }
}