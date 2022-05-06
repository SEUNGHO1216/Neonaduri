//package com.sparta.neonaduri_back.dto.user;
//
///**
// * [dto] - [user] 카카오 로그인 유저 정보 KakaoUserInfoDto
// *
// * @class   : KakaoUserInfoDto
// * @author  : 오예령
// * @since   : 2022.05.03
// * @version : 1.0
// *
// *   수정일     수정자             수정내용
// *  --------   --------    ---------------------------
// *  2022.05.03 오예령       dto 안에 user 패키지 만들어서 관련 class 합쳐놓음
// *  2022.05.04 오예령      카카오 토큰에 totalLike 항목 추가, email data userName 으로 통합
// *  2022.05.06 오예령      SignupRequestDto로 통합시켜서 삭제해도 됩니다!
// */
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//
//@Getter
//@AllArgsConstructor
//public class KakaoUserInfoDto {
//    private Long id;
//    private String userName;
//    private String nickName;
//    private String profileImgUrl;
//    private String email;
//    private int totalLike;
//
//    // 카카오 로그인
//    public KakaoUserInfoDto(String userName, String nickName, String profileImgUrl, int totalLike) {
//        this.userName = userName;
//        this.nickName = nickName;
//        this.profileImgUrl = profileImgUrl;
//        this.totalLike = totalLike;
//    }
//
//}
