package com.sparta.neonaduri_back.dto.user;

/**
 * [dto] - [user] 로그인 회원 정보 조회 IsLoginDto
 *
 * @class   : IsLoginDto
 * @author  : 오예령
 * @since   : 2022.05.03
 * @version : 1.0
 *
 *   수정일     수정자             수정내용
 *  --------   --------    ---------------------------
 *  2022.05.03 오예령       dto 안에 user 패키지 만들어서 관련 class 합쳐놓음
 *  2022.05.04 오예령       email 추가
 */

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IsLoginDto {
    private String userName;
    private String nickName;
    private String profileImgUrl;
    private int totalLike;
    private String email;

}
