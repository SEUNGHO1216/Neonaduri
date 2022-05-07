package com.sparta.neonaduri_back.repository;

import com.sparta.neonaduri_back.model.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByPostIdAndUserId(Long postId, Long userId);

    void deleteByPostIdAndUserId(Long postId, Long userId);

    Long countByPostId(Long postId);

    List<Likes> findAllByUserIdOrderByModifiedAtDesc(Long userId);
}
