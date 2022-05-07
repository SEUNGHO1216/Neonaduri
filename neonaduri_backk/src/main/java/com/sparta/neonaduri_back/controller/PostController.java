package com.sparta.neonaduri_back.controller;

import com.sparta.neonaduri_back.dto.post.*;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.service.PostService;
import com.sparta.neonaduri_back.utils.StatusEnum;
import com.sparta.neonaduri_back.utils.StatusMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;

    //방 만들어주기
    @PostMapping("/api/makeplan")
    public RoomMakeRequestDto makeRoom(@RequestBody RoomMakeRequestDto roomMakeRequestDto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user= userDetails.getUser();
        return postService.makeRoom(roomMakeRequestDto, user);
    }

    //자랑하기, 나만보기 저장
    @PutMapping("/api/saveplan")
    public ResponseEntity<StatusMessage> showAll(@RequestBody PostRequestDto postRequestDto,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails){
        User user=userDetails.getUser();
        Long postId=postService.showAll(postRequestDto, user);
        if(postRequestDto.getPostId()==postId){

            StatusMessage message= new StatusMessage();
            message.setStatus(StatusEnum.OK);
            message.setData("자랑 성공");
            return new ResponseEntity<StatusMessage>(message, HttpStatus.OK);
        }else{
            StatusMessage message= new StatusMessage();
            message.setStatus(StatusEnum.BAD_REQUEST);
            message.setData("자랑 실패");
            return new ResponseEntity<StatusMessage>(message, HttpStatus.BAD_REQUEST);

        }
    }

    //내가 찜한 여행목록 조회
    @GetMapping("/api/user/mypage/like/{pageno}")
    public MyLikeResponseDto showMyLike(@PathVariable("pageno") int pageno,@AuthenticationPrincipal UserDetailsImpl userDetails){

        //MyLikePostDto
        Page<MyLikePostDto> postList=postService.showMyLike(pageno-1,userDetails);

        //totalLike
        int totalLike=postService.getTotalLike(userDetails);

        //islastPage
        boolean islastPage=false;
        if(postList.getTotalPages()==postList.getNumber()+1){
            islastPage=true;
        }
        System.out.println(postList.getNumber()+1);
        MyLikeResponseDto myLikeResponseDto = new MyLikeResponseDto(totalLike, postList, islastPage);
        return myLikeResponseDto;
    }

    //인기 게시물 4개 조회
    @GetMapping("/api/planning/best")
    public List<BestAndLocationDto> showBestPosts(@AuthenticationPrincipal UserDetailsImpl userDetails){

        return postService.showBestPosts(userDetails);
    }

    //지역별 조회(20개)
    @GetMapping("/api/planning/location/{location}")
    public List<BestAndLocationDto> showLocationPosts(@PathVariable String location, @AuthenticationPrincipal UserDetailsImpl userDetails){
        return postService.showLocationPosts(location,userDetails);
    }

}
