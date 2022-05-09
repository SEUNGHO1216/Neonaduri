package com.sparta.neonaduri_back.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PlaceRequestDto {

    private String placeName;
    private String placeInfoUrl;
    private String category;
    private String address;
    private String roadAddress;
    private String placeMemo;
    private String lat;
    private String lng;
}
