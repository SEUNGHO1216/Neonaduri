package com.sparta.neonaduri_back.repository;

import com.sparta.neonaduri_back.model.Post;
import com.sparta.neonaduri_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    //내가 찜한 여행 목록 조회
    List<Post> findAllByUserOrderByModifiedAtDesc(User user);

    // 상세 조회, 계획 저장 전 삭제
    Optional<Post> findByPostId(Long postId);

    List<Post> findAllByUser(User user);
}
