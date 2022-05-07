package com.sparta.neonaduri_back.service;

/**
 * [Service] - 후기 등록 ReviewService
 *
 * @class   : ReviewService
 * @author  : 오예령
 * @since   : 2022.05.07
 * @version : 1.0
 *
 *   수정일     수정자             수정내용
 *  --------   --------    ---------------------------
 *
 */

import com.sparta.neonaduri_back.dto.review.ReviewRequestDto;
import com.sparta.neonaduri_back.model.Review;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    // 후기 등록
    public void createReview(Long postId,ReviewRequestDto reviewRequestDto, User user) {

        String reviewContents = reviewRequestDto.getReviewContents();
        String reviewImgUrl = reviewRequestDto.getReviewImgUrl();

        Review review = new Review(reviewContents, reviewImgUrl,user, postId);

        // 후기 저장
        reviewRepository.save(review);
    }
}
