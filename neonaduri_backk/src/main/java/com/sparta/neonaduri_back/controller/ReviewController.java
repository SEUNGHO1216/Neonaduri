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

import com.sparta.neonaduri_back.dto.review.ReviewListDto;
import com.sparta.neonaduri_back.dto.review.ReviewRequestDto;
import com.sparta.neonaduri_back.dto.review.ReviewResponseDto;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.service.ReviewService;
import com.sparta.neonaduri_back.service.S3Uploader;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@AllArgsConstructor
@RequestMapping("/api/detail/reviews")
@RestController
public class ReviewController {
    private final ReviewService reviewService;
    private final S3Uploader S3Uploader;

    // 후기 등록
    @PostMapping("/{postId}")
    public ResponseEntity<String> createReview(@PathVariable Long postId,
                                               @RequestParam("reviewContents") String reviewContetns,
                                               @RequestParam(value = "reviewImgUrl")MultipartFile multipartFile,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws IOException {

        String reviewImgUrl = S3Uploader.upload(multipartFile, "static");

        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(reviewContetns, reviewImgUrl);
        reviewService.createReview(postId, reviewRequestDto, userDetails.getUser());
        return ResponseEntity.status(201)
                .body("201");
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
        return ResponseEntity.status(201)
                .body(reviewResponseDto);
    }
}
