package com.creatorboard.repository;

import com.creatorboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // ログイン時にユーザー名で検索するために追加
    Optional<User> findByUsername(String username);
}