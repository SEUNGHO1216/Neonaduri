package com.sparta.neonaduri_back.controller;

import com.sparta.neonaduri_back.dto.post.*;
import com.sparta.neonaduri_back.dto.review.MyReviewListDto;
import com.sparta.neonaduri_back.model.Post;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.service.PostService;
import com.sparta.neonaduri_back.service.ReviewService;
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
    private final ReviewService reviewService;

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
    public MyLikePagingDto showMyLike(@PathVariable("pageno") int pageno, @AuthenticationPrincipal UserDetailsImpl userDetails){

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
        MyLikePagingDto myLikePagingDto = new MyLikePagingDto(totalLike, postList, islastPage);
        return myLikePagingDto;
    }

    //인기 게시물 4개 조회
    @GetMapping("/api/planning/best")
    public List<BestAndLocationDto> showBestPosts(@AuthenticationPrincipal UserDetailsImpl userDetails){

        return postService.showBestPosts(userDetails);
    }

    //지역별 조회(8개)
    @GetMapping("/api/planning/location/{location}/{pageno}")
    public BestAndLocationPagingDto showLocationPosts(@PathVariable("location") String location,@PathVariable("pageno") int pageno,
                                                      @AuthenticationPrincipal UserDetailsImpl userDetails){

        //BestAndLocationDto
        Page<BestAndLocationDto> postList=postService.showLocationPosts(location,pageno-1,userDetails);

        //islastPage
        boolean islastPage=false;
        if(postList.getTotalPages()==postList.getNumber()+1){
            islastPage=true;
        }
        BestAndLocationPagingDto bestAndLocationPagingDto=new BestAndLocationPagingDto(postList,islastPage);
        return  bestAndLocationPagingDto;
    }

    //테마별 조회
    @GetMapping("/api/planning/theme/{theme}/{pageno}")
    public ThemeAndSearchPagingDto showThemePosts(@PathVariable("theme") String theme, @PathVariable("pageno") int pageno,
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails){
        Page<ThemeAndSearchDto> postList=postService.showThemePosts(theme,pageno-1,userDetails);

        //islastPage
        boolean islastPage=false;
        if(postList.getTotalPages()==postList.getNumber()+1){
            islastPage=true;
        }
        ThemeAndSearchPagingDto themeAndSearchPagingDto=new ThemeAndSearchPagingDto(postList, islastPage);
        return themeAndSearchPagingDto;
    }

    //상세조회
    @GetMapping("/api/detail/{postId}")
    public ResponseEntity<Object> showDetail(@PathVariable("postId") Long postId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        Post post=postService.showDetail(postId, userDetails);
        if(post==null){
            return ResponseEntity.status(200).body("비공개 게시물입니다");
        }
        return ResponseEntity.ok(post);
    }

    //내가 등록한 여행 계획 삭제
    @DeleteMapping("/api/user/delplan/{postId}")
    public ResponseEntity<StatusMessage> deletePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @PathVariable("postId") Long postId){
        Long deletedPostId=postService.deletePost(userDetails, postId);
        if(postId==deletedPostId){
            StatusMessage statusMessage=new StatusMessage();
            statusMessage.setStatus(StatusEnum.OK);
            statusMessage.setData("삭제가 정상적으로 완료됨");
            return new ResponseEntity<StatusMessage>(statusMessage,HttpStatus.OK);
        }else{
            StatusMessage statusMessage=new StatusMessage();
            statusMessage.setStatus(StatusEnum.BAD_REQUEST);
            statusMessage.setData("삭제 실패");
            return new ResponseEntity<StatusMessage>(statusMessage, HttpStatus.BAD_REQUEST);
        }
    }

    //검색 결과 조회
    @GetMapping("/api/search/{keyword}/{pageno}")
    public ThemeAndSearchPagingDto showSearchPosts(@PathVariable("pageno") int pageno, @PathVariable("keyword") String keyword,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails){
        System.out.println("키워드:"+keyword);
        Page<ThemeAndSearchDto> postList=postService.showSearchPosts(pageno-1, keyword, userDetails);

        //islastPage
        boolean islastPage=false;
        if(postList.getTotalPages()==postList.getNumber()+1){
            islastPage=true;
        }
        ThemeAndSearchPagingDto themeAndSearchPagingDto=new ThemeAndSearchPagingDto(postList, islastPage);
        return themeAndSearchPagingDto;
    }

    //플랜 저장 안함.(새로고침 뒤로가기)
    @DeleteMapping("/api/makeplan/{postId}")
    public ResponseEntity<String> leavePost(@PathVariable Long postId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        return postService.leavePost(postId, user);
    }

    //플랜 계획 조회하기
    @GetMapping("/api/makeplan/{postId}")
    public RoomMakeRequestDto getPost(@PathVariable Long postId) {
        return postService.getPost(postId);
    }

    //내가 작성한 플랜조회
    @GetMapping("/api/user/getplan/{pageno}")
    public PostResponseDto getMyPost(@PathVariable("pageno") int pageno,
                                @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Page<PostListDto> myplanList = postService.getMyPosts(pageno-1,userDetails);

        boolean islastPage = false;
        if ( myplanList.getTotalPages() == myplanList.getNumber()+1){
            islastPage=true;
        }
        PostResponseDto postList = new PostResponseDto(myplanList, islastPage);
        return postList;
    }

    // 내가 쓴 후기 조회
    @GetMapping("/api/user/mypage/review")
    public List<MyReviewListDto> showMyReviews(@AuthenticationPrincipal UserDetailsImpl userDetails){
        return reviewService.showMyReviews(userDetails);
    }

}
