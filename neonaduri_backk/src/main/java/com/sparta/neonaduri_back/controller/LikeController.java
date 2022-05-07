package com.sparta.neonaduri_back.controller;

import com.sparta.neonaduri_back.dto.LikeResponseDto;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class LikeController {

    private final LikeService likeService;

    //찜 여부 확인 (찜 상태 - true, 찜 안 한 상태 - false)
    @PostMapping("/api/posts/like/{postId}")
    public LikeResponseDto isLike(@PathVariable Long postId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        User user=userDetails.getUser();
        return likeService.toggle(postId, user);
    }
}
