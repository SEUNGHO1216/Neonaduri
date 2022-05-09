package com.sparta.neonaduri_back.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MyLikePagingDto {
    private int totalLike;
    private List<MyLikePostDto> postList=new ArrayList<>();
    private int totalPage;
    private boolean islastPage;

    public MyLikePagingDto(int totalLike, Page<MyLikePostDto> postDtoList
    , boolean islastPage) {
        this.totalLike=totalLike;
        this.postList= postDtoList.getContent();
        this.totalPage= postDtoList.getTotalPages();
        this.islastPage=islastPage;
    }
}
