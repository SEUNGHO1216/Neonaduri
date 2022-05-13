package com.sparta.neonaduri_back.controller;

/**
 * [controller] - reviewController
 *
 * @class   : reviewController
 * @author  : 오예령
 * @since   : 2022.05.07
 * @version : 1.0
 *
 *   수정일     수정자             수정내용
 *  --------   --------    ---------------------------
 *
 */

import com.sparta.neonaduri_back.dto.review.*;
import com.sparta.neonaduri_back.model.Review;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.service.ReviewService;
import com.sparta.neonaduri_back.service.S3Uploader;
import jdk.net.SocketFlow;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@RequestMapping("/api/detail/reviews")
@RestController
public class ReviewController {
    private final ReviewService reviewService;
    private final S3Uploader S3Uploader;

    // 후기 등록
    @PostMapping("/{postId}")
    public ResponseEntity<ReviewListDto> createReview(@PathVariable("postId") Long postId,
                                                                 @RequestParam(value = "reviewContents") String reviewContents,
                                                                 @RequestParam(value = "reviewImgFile")MultipartFile multipartFile,
                                                                 @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws IOException {

        ReviewListDto reviewListDto;
        if(multipartFile.isEmpty()){
            reviewListDto=reviewService.createReviewOnlyContents(postId, reviewContents, userDetails.getUser());
        }else{
            String reviewImgUrl = S3Uploader.upload(multipartFile, "static");
            ReviewRequestDto reviewRequestDto = new ReviewRequestDto(reviewContents, reviewImgUrl);
            reviewListDto=reviewService.createReview(postId, reviewRequestDto, userDetails.getUser());
        }

        //responseEntity.status().body()형태로 담아줘야 json형식으로 간다.
        return ResponseEntity.status(201).body(reviewListDto);
    }

    // 후기 조회 - 페이징 처리
    @GetMapping("/{postId}/{pageno}")
    public ResponseEntity<ReviewResponseDto> getReviews(@PathVariable(value = "postId") Long postId, @PathVariable(value = "pageno") int pageno, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // reviewService.getReviews(postId, pageno-1, userDetails);
        Page<ReviewListDto> reviewList = reviewService.getReviews(postId, pageno-1);

        // islastPage
        boolean islastPage=false;
        if(reviewList.getTotalPages()==reviewList.getNumber()+1){
            islastPage=true;
        }
        ReviewResponseDto reviewResponseDto = new ReviewResponseDto(reviewList, islastPage);
        return ResponseEntity.status(200)
                .body(reviewResponseDto);
    }

    //후기 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<String> updateReviews(@PathVariable("reviewId") Long reviewId,
                                                           @RequestParam("reviewImgUrl") String reviewImgUrl,
                                                           @RequestParam("reviewImgFile") MultipartFile multipartFile,
                                                           @RequestParam("reviewContents") String reviewContents,
                                                           @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        //파일을 보내지 않은 경우 -> url만 보냈거나, url도 보내지 않은 경우
        if(multipartFile.isEmpty()){
            reviewService.updateReview(reviewId,reviewImgUrl,reviewContents,userDetails);
        }else{
            System.out.println(reviewContents);
            reviewService.updateReviewWithFile(reviewId, multipartFile, reviewContents, userDetails);
        }
        return ResponseEntity.status(201).body("201");
    }

    //후기 수정 전 다시 조회
    @GetMapping("/edit/{reviewId}")
    public ResponseEntity<ReviewListDto> getReviewAgain(@PathVariable("reviewId") Long reviewId,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails){
        ReviewListDto reviewListDto=reviewService.getReviewAgain(reviewId, userDetails);

        return ResponseEntity.status(200).body(reviewListDto);
    }

    //후기 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Object> deleteReview(@PathVariable("reviewId") Long reviewId,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails){
        reviewService.deleteReview(reviewId,userDetails);
        return ResponseEntity.status(200).body("200");
    }

}
