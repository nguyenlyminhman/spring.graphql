package com.spring.graphql.service;

import com.spring.graphql.dto.Inputs.*;
import com.spring.graphql.model.Post;
import com.spring.graphql.model.User;
import com.spring.graphql.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<Post> findAll() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Post> findByAuthor(User author) {
        return postRepository.findAllByAuthorOrderByCreatedAtDesc(author);
    }

    public Post create(CreatePostInput input, User author) {
        Post post = Post.builder()
                .title(input.title())
                .content(input.content())
                .author(author)
                .build();
        return postRepository.save(post);
    }
}
