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

import com.sparta.neonaduri_back.dto.review.ReviewRequestDto;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.service.ReviewService;
import com.sparta.neonaduri_back.service.S3Uploader;
import lombok.AllArgsConstructor;
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
//
//    @GetMapping("/{postId}/{pageno}")
//    public ResponseEntity<>
}
