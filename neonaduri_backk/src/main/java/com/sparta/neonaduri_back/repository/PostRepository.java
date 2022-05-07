package com.sparta.neonaduri_back.repository;

import com.sparta.neonaduri_back.model.Post;
import com.sparta.neonaduri_back.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    //내가 찜한 여행 목록 조회
    List<Post> findAllByUserOrderByModifiedAtDesc(User user);

    //찜한 개수 순으로 조회(ispublic=true) 중에!
    List<Post> findAllByIspublicTrueOrderByLikeCntDesc();

    //방 생선한 사람이 저장하는게 아니면 예외처리
    Optional<Post> findByUserAndPostId(User user, Long postId);

    //지역'만'으로 찾기
    List<Post> findAllByLocationOrderByLikeCntDesc(String location);
}
