package com.sparta.neonaduri_back.repository;

import com.sparta.neonaduri_back.model.Post;
import com.sparta.neonaduri_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    //내가 찜한 여행 목록 조회
    List<Post> findAllByUserOrderByModifiedAtDesc(User user);

}
