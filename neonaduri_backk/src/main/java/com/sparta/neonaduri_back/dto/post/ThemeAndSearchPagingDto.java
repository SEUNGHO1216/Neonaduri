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
public class ThemeAndSearchPagingDto {

    private List<ThemeAndSearchDto> resultList=new ArrayList<>();
    private int totalPage;
    private boolean islastPage;

    public ThemeAndSearchPagingDto(Page<ThemeAndSearchDto> postDtoList, boolean islastPage) {

        this.resultList = postDtoList.getContent();
        this.totalPage = postDtoList.getTotalPages();
        this.islastPage = islastPage;
    }
}