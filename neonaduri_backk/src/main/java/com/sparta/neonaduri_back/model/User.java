package com.sparta.neonaduri_back.model;

/**
 * [model] - User
 *
 * @class   : User
 * @author  : 오예령
 * @since   : 2022.04.30
 * @version : 1.0
 *
 *   수정일     수정자             수정내용
 *  --------   --------    ---------------------------
 *  2022.05.03 오예령       회원 정보에 profileImgUrl, totalLike 추가
 */

import com.sparta.neonaduri_back.dto.user.SignupRequestDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickName;

//    @Column(nullable = true)
//    private String email;

//    @Column(nullable = true)
//    private Long kakaoId;

    @Column(nullable = true)
    private String profileImgUrl;

    @Column(nullable = true)
    private int totalLike;

    // 카카오 회원가입
    public User(String userName, String nickName, String password, String profileImgUrl) {
        this.userName = userName;
        this.nickName = nickName;
        this.password = password;
        this.profileImgUrl = profileImgUrl;
    }

    // 구글 회원가입
    @Builder
    public User(String userName, String nickName, String password, String profileImgUrl, int totalLike) {
        this.userName = userName;
        this.nickName = nickName;
        this.password = password;
        this.profileImgUrl = profileImgUrl;
        this.totalLike = totalLike;
    }

    // 회원가입
    public User(String userName, String password, SignupRequestDto signupRequestDto) {
        this.userName = userName;
        this.password = password;
        this.nickName = signupRequestDto.getNickName();
        this.profileImgUrl = "https://seunghodev-bucket.s3.ap-northeast-2.amazonaws.com/%EA%B0%9C%ED%97%88%ED%83%88.jpg";
    }

    //회원 프로필 업데이트
    public void update(String profileImgUrl, String nickName){
        this.profileImgUrl=profileImgUrl;
        this.nickName=nickName;
    }

}