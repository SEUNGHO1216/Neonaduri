package com.sparta.neonaduri_back.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ThemeAndSearchDto {

    private Long postId;
    private String postImgUrl;
    private String postTitle;
    private String location;
    private boolean islike;
    private int likeCnt;
    private int reviewCnt;
    private String theme;
}
