package com.sparta.neonaduri_back.service;

/**
 * [Service] - 회원가입 Service
 *
 * @class   : UserService
 * @author  : 오예령
 * @since   : 2022.04.30
 * @version : 1.0
 *
 *   수정일     수정자             수정내용
 *  --------   --------    ---------------------------
 *  2022.05.03 오예령       아이디 중복검사 및 로그인 정보 조회 기능 추가
 *  2022.05.04 오예령       유저 정보 조회 항목에 email 추가
 */


import com.sparta.neonaduri_back.dto.user.SignupRequestDto;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.repository.UserRepository;
import com.sparta.neonaduri_back.validator.UserInfoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashMap;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passWordEncoder;
    private final UserInfoValidator userInfoValidator;
    private final S3Uploader S3uploader;

    //회원가입
   @Transactional
    public String  registerUser(SignupRequestDto signupRequestDto, Errors errors){
        String message = userInfoValidator.getValidMessage(signupRequestDto, errors);
        if(message.equals("회원가입 성공")){
            String userName = signupRequestDto.getUserName();
            //비밀번호 암호화
            String passWordEncode = passWordEncoder.encode(signupRequestDto.getPassword());
            //저장할 유저 객체 생성
            User user = new User(userName, passWordEncode, signupRequestDto);
            //회원정보 저장
             userRepository.save(user);
             return "회원가입 성공";
        }else{
            return message;
        }
    }

    //유저 프로필 수정
    @Transactional
    public void updateUserInfo(MultipartFile multipartFile, String nickName, Long userId) throws IOException {

        String profileImgUrl=S3uploader.updateImage(multipartFile, "static", userId);


        User user=userRepository.findById(userId).orElseThrow(
                ()->new IllegalArgumentException("회원 정보가 없습니다")
        );
        user.update(profileImgUrl, nickName);
        userRepository.save(user);
    }

    //사진 삭제했거나 이미지 수정 안했을 시
    @Transactional
    public void deleteProfileImg(String profileImgUrl, String nickName, Long userId) {

        //사진 삭제시 -> 기본이미지로 변경
        if(profileImgUrl.equals("")){
            User user = userRepository.findById(userId).orElseThrow(
                    ()->new IllegalArgumentException("해당 유저가 없습니다")
            );
            //디폴트 이미지
            profileImgUrl="https://seunghodev-bucket.s3.ap-northeast-2.amazonaws.com/%EA%B0%9C%ED%97%88%ED%83%88.jpg";
            user.update(profileImgUrl,nickName);
            userRepository.save(user);
        }
        //수정 안했을 시(url은 존재하는 경우)
        else{
            User user = userRepository.findById(userId).orElseThrow(
                    ()->new IllegalArgumentException("해당 유저가 없습니다")
            );
            user.update(profileImgUrl, nickName);
            userRepository.save(user);
        }
    }

}
