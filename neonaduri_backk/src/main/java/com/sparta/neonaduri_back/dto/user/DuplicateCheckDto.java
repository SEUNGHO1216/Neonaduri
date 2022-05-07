package com.sparta.neonaduri_back.dto.user;

/**
 * [dto] - [user] 아이디 중복검사 DuplicateCheckDto
 *
 * @class   : DuplicateCheckDto
 * @author  : 오예령
 * @since   : 2022.05.03
 * @version : 1.0
 *
 *   수정일     수정자             수정내용
 *  --------   --------    ---------------------------
 *  2022.05.03 오예령       dto 안에 user 패키지 만들어서 관련 class 합쳐놓음
 */


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
public class DuplicateCheckDto {
    private String userName;
}
