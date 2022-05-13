package com.sparta.neonaduri_back.dto.review;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@NoArgsConstructor
@Getter
public class ReviewResponseDto {
    private List<ReviewListDto> reviewList;
    private int totalPage;
    private int totalElements;
    private boolean islastPage;

    public ReviewResponseDto(Page<ReviewListDto> reviewList, boolean islastPage) {
        this.reviewList = reviewList.getContent();
        this.totalPage = reviewList.getTotalPages();
        this.totalElements = (int)reviewList.getTotalElements();
        this.islastPage = islastPage;

    }
}
