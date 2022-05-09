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

import com.sparta.neonaduri_back.dto.review.ReviewListDto;
import com.sparta.neonaduri_back.dto.review.ReviewRequestDto;
import com.sparta.neonaduri_back.model.Review;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.repository.PostRepository;
import com.sparta.neonaduri_back.repository.ReviewRepository;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.validator.UserInfoValidator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final UserInfoValidator validator;

    // 후기 등록
    public void createReview(Long postId,ReviewRequestDto reviewRequestDto, User user) {

        String reviewContents = reviewRequestDto.getReviewContents();
        String reviewImgUrl = reviewRequestDto.getReviewImgUrl();

        Review review = new Review(reviewContents, reviewImgUrl,user, postId);

        // 후기 저장
        reviewRepository.save(review);
    }

    // 후기 조회
    public Page<ReviewListDto> getReviews(Long postId, int pageno) {

        // 후기 list 빈 배열 선언
        List<ReviewListDto> reviewList = new ArrayList<>();

        // postId로 해당 post의 후기 list 가져오기
        List<Review> reviews = reviewRepository.findAllByPostId(postId);

        Pageable pageable = getPageable(pageno);

        for (Review review : reviews) {

            ReviewListDto reviewListDto = new ReviewListDto(review.getId(), review.getUser().getNickName(), review.getReviewContents(),
                    review.getReviewImgUrl(), review.getCreatedAt(), review.getModifiedAt());

            reviewList.add(reviewListDto);
        }

        int start = pageno * 4;
        int end =  Math.min((start + 4), reviewList.size());

        return validator.overPages3(reviewList, start, end, pageable, pageno);
    }

    //페이징
    private Pageable getPageable(int pageno) {
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "id");
        return PageRequest.of(pageno, 4, sort);
    }
}
