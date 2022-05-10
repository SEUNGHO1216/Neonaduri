package com.sparta.neonaduri_back.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostListDto {
    private Long postId;
    private String postImgUrl;
    private String startDate;
    private String endDate;
    private String postTitle;
    private String location;
    private String theme;
    private boolean islike;
    private boolean ispublic;
    private int likeCnt;
    private int reviewCnt;

}
