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
