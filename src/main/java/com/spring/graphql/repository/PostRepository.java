package com.spring.graphql.repository;

import com.spring.graphql.model.Post;
import com.spring.graphql.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByAuthorOrderByCreatedAtDesc(User author);
    List<Post> findAllByOrderByCreatedAtDesc();
}
