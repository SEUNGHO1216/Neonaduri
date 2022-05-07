package com.sparta.neonaduri_back.dto.post;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class PostRequestDto {

    private Long postId;
    private String startDate;
    private String endDate;
    private String dateCnt;
    private String postTitle;
    private String location;
    private boolean ispublic;
    private String theme;
    private boolean islike;
    private List<DayRequestDto> days=new ArrayList<>();

}
