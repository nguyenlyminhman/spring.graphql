package com.spring.graphql.repository;

import com.spring.graphql.model.RefreshToken;
import com.spring.graphql.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    // Dùng @Modifying + @Query để xóa trực tiếp bằng JPQL (hiệu quả hơn load rồi xóa)
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteAllByUser(User user);
}
