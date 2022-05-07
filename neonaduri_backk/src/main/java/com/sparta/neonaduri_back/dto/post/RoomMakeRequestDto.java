package com.sparta.neonaduri_back.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
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
