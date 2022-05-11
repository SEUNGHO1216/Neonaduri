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

import com.sparta.neonaduri_back.dto.review.MyReviewListDto;
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
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final UserInfoValidator validator;
    private final S3Uploader s3Uploader;

    /*  private Long reviewId;
    private String nickName;
    private String reviewContents;
    private String reviewImgUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;*/
    // 후기 등록
    public ReviewListDto createReview(Long postId,ReviewRequestDto reviewRequestDto, User user) {

        String reviewContents = reviewRequestDto.getReviewContents();
        String reviewImgUrl = reviewRequestDto.getReviewImgUrl();

        Review review = new Review(reviewContents, reviewImgUrl,user, postId);
        // 후기 저장
        reviewRepository.save(review);

        ReviewListDto reviewListDto=new ReviewListDto(review.getId(), user.getNickName(), reviewContents, reviewImgUrl,
                review.getCreatedAt(), review.getModifiedAt());
        return reviewListDto;
    }

    // 내용만 등록시
    public ReviewListDto createReviewOnlyContents(Long postId, String reviewContents, User user) {

        Review review=new Review(reviewContents,user, postId);
        reviewRepository.save(review);
        ReviewListDto reviewListDto=new ReviewListDto(review.getId(), user.getNickName(), reviewContents, review.getReviewImgUrl(),
                review.getCreatedAt(), review.getModifiedAt());
        return reviewListDto;
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

    //후기수정(URL)
    @Transactional
    public void updateReview(Long reviewId, String reviewImgUrl, String reviewContents, UserDetailsImpl userDetails) {

        Review review=reviewRepository.findById(reviewId).orElseThrow(
                ()-> new IllegalArgumentException("해당 리뷰가 없습니다")
        );
        if(review.getUser().getId()!=userDetails.getUser().getId()){
            throw new IllegalArgumentException("리뷰 작성자만 수정이 가능합니다");
        }
        //이미지 유알엘도 없다 -> 사진도 지운다
        if(reviewImgUrl.equals("")){

            review.update(reviewContents, reviewImgUrl);
            reviewRepository.save(review);

        }else{
            //이미지 수정 x 기존값 그대로
            review.update(reviewContents,reviewImgUrl);
            reviewRepository.save(review);
        }
    }

    //후기수정(사진 파일)
    @Transactional
    public void updateReviewWithFile(Long reviewId, MultipartFile multipartFile, String reviewContents,UserDetailsImpl userDetails) throws IOException {

        System.out.println("서비스단내용"+reviewContents);
        Review review=reviewRepository.findById(reviewId).orElseThrow(
                ()->new IllegalArgumentException("해당 리뷰가 없습니다")
        );
        if(review.getUser().getId()!=userDetails.getUser().getId()){
            throw new IllegalArgumentException("리뷰 작성자만 수정이 가능합니다");
        }
        String reviewImgUrl=s3Uploader.updateReviewImage(multipartFile,"static",reviewId);
        System.out.println("url"+reviewImgUrl);
        review.update(reviewContents, reviewImgUrl);
        reviewRepository.save(review);
    }


    public ReviewListDto getReviewAgain(Long reviewId, UserDetailsImpl userDetails) {
        Review review=reviewRepository.findById(reviewId).orElseThrow(
                ()-> new IllegalArgumentException("해당 리뷰가 없습니다")
        );

        String nickName=review.getUser().getNickName();
        String reviewContents=review.getReviewContents();
        String reviewImgUrl=review.getReviewImgUrl();
        LocalDateTime createdAt=review.getCreatedAt();
        LocalDateTime modifiedAt=review.getModifiedAt();

        ReviewListDto reviewListDto=new ReviewListDto(reviewId, nickName,reviewContents,
                reviewImgUrl,createdAt,modifiedAt);
        return reviewListDto;
    }

    //후기 삭제
    @Transactional
    public Long deleteReview(Long reviewId, UserDetailsImpl userDetails) {

        Review review=reviewRepository.findById(reviewId).orElseThrow(
                ()->new IllegalArgumentException("해당 리뷰가 없습니다")
        );
        if(review.getUser().getId()!=userDetails.getUser().getId()){
            throw new IllegalArgumentException("리뷰 작성자만 삭제가 가능합니다");
        }
        reviewRepository.deleteById(reviewId);
        return reviewId;
    }

    //내가 쓴 후기 조회
    public List<MyReviewListDto> showMyReviews(UserDetailsImpl userDetails) {
        User user=userDetails.getUser();
        List<Review> reviewList=reviewRepository.findAllByUser(user);

        List<MyReviewListDto> myReviewList=new ArrayList<>();

        for(Review eachReview:reviewList){
            MyReviewListDto myReviewListDto=new MyReviewListDto(eachReview.getId(), eachReview.getPostId(),
                    eachReview.getUser().getNickName(), eachReview.getReviewContents(), eachReview.getReviewImgUrl(),
                    eachReview.getCreatedAt(), eachReview.getModifiedAt());
            myReviewList.add(myReviewListDto);
        }
        return myReviewList;
    }
}

