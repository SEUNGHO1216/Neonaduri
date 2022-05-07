package com.sparta.neonaduri_back.dto.post;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RoomMakeRequestDto {

    private Long postId;
    private String startDate;
    private String endDate;
    private int dateCnt;
    private String postTitle;
    private String location;
    private String theme;


}

/*startDate :  2022-04-28
endDate : 2022-04-30
dateCnt : 3,
title : 부산 여행
location : 부산,
theme : 액티비티*/