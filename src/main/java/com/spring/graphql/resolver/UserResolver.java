package com.spring.graphql.resolver;

import com.spring.graphql.annotation.RequireLogin;
import com.spring.graphql.config.GraphQLContextInterceptor;
import com.spring.graphql.dto.Inputs.*;
import com.spring.graphql.model.Post;
import com.spring.graphql.model.User;
import com.spring.graphql.service.PostService;
import graphql.GraphQLContext;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserResolver {

    private final PostService postService;

    // PRIVATE - có @RequireLogin
    // PUBLIC — không có @RequireLogin


    // ============================================================================
    // Queries
    // ============================================================================

    @QueryMapping
    public String publicInfo() {
        return "Đây là thông tin công khai, không cần đăng nhập!";
    }

    @QueryMapping
    @RequireLogin
    public User me(GraphQLContext ctx) {
        // Vào được đây = chắc chắn đã login (AOP đã kiểm tra)
        return ctx.get(GraphQLContextInterceptor.CURRENT_USER_KEY);
    }

    @QueryMapping
    @RequireLogin
    public List<Post> myPosts(GraphQLContext ctx) {
        User user = ctx.get(GraphQLContextInterceptor.CURRENT_USER_KEY);
        return postService.findByAuthor(user);
    }

    @QueryMapping
    @RequireLogin
    public List<Post> allPosts(GraphQLContext ctx) {
        // Không cần dùng user, nhưng vẫn cần GraphQLContext cho @RequireLogin Aspect
        return postService.findAll();
    }


    // ============================================================================
    // Mutations
    // ============================================================================

    @MutationMapping
    @RequireLogin
    public Post createPost(@Argument CreatePostInput input, GraphQLContext ctx) {
        User user = ctx.get(GraphQLContextInterceptor.CURRENT_USER_KEY);
        return postService.create(input, user);
    }
}