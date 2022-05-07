package com.sparta.neonaduri_back.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class MyLikePostDto {

    private Long postId;
    private String postImg;
    private String postTitle;
    private String location;
    private String startDate;
    private String endDate;
    private boolean islike;
    private int likeCnt;
    private String theme;
}
