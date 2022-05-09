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
public class BestAndLocationPagingDto {

    private List<BestAndLocationDto> locationList=new ArrayList<>();
    private int totalPage;
    private boolean islastPage;

    public BestAndLocationPagingDto(Page<BestAndLocationDto> postDtoList, boolean islastPage) {

        this.locationList= postDtoList.getContent();
        this.totalPage= postDtoList.getTotalPages();
        this.islastPage=islastPage;
    }
}
