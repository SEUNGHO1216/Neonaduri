package com.sparta.neonaduri_back.service;

import com.sparta.neonaduri_back.dto.LikeResponseDto;
import com.sparta.neonaduri_back.model.Likes;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;

    @Transactional
    public LikeResponseDto toggle(Long postId, User user) {
        Long userId=user.getId();
        Optional<Likes> likesOptional=likeRepository.findByPostIdAndUserId(postId, userId);

        LikeResponseDto likeResponseDto = new LikeResponseDto();
        //이미 해당 게시물 찜한 경우
        if(likesOptional.isPresent()){
            //찜한 내역 삭제
            likeRepository.deleteByPostIdAndUserId(postId, userId);
            //찜 false 상태 반환
            likeResponseDto.setLike(false);
        }else{
        //아직 찜 안 한 경우
            Likes likes=new Likes(userId, postId);
            likeRepository.save(likes);
            likeResponseDto.setLike(true);
        }
        return likeResponseDto;
    }
}
