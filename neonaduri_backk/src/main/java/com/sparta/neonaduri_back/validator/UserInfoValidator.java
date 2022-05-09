package com.sparta.neonaduri_back.validator;

/**
 * [validator] - 유효성 검사
 *
 * @class   : UserInfoValidator
 * @author  : 오예령
 * @since   : 2022.04.30
 * @version : 1.0
 *
 *   수정일     수정자             수정내용
 *  --------   --------    ---------------------------
 *  2022.05.06 오예령       유효성 검사 추가, 아이디 중복 체크
 *  2022.05.07 오예령       유저 정보 조회 return 형태 변경 (리팩토링)
 */


import com.sparta.neonaduri_back.dto.post.MyLikePostDto;
import com.sparta.neonaduri_back.dto.post.PostListDto;
import com.sparta.neonaduri_back.dto.review.ReviewListDto;
import com.sparta.neonaduri_back.dto.user.IsLoginDto;
import com.sparta.neonaduri_back.dto.user.SignupRequestDto;
import com.sparta.neonaduri_back.model.Post;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.repository.UserRepository;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@RestControllerAdvice
@Component
public class UserInfoValidator {

    private final UserRepository userRepository;

    public UserInfoValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getValidMessage(SignupRequestDto signupRequestDto, Errors errors) {

        String regex = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";
        String str = signupRequestDto.getUserName();

        if (errors.hasErrors()) {
            Map<String, String> validatorResult = validateHandling(errors);
            return validatorResult.get("message");
        } else if (!Pattern.matches(regex,str)) {
            return "올바른 이메일 형식이 아닙니다.";
        } else if (userRepository.findByUserName(signupRequestDto.getUserName()).isPresent()) {
            return "중복된 아이디가 존재합니다.";
        } else if (signupRequestDto.getPassword().length() < 4) {
            return "비밀번호는 4자리 이상 12자리 미만입니다.";
        } else {
            return "회원가입 성공";
        }
    }

    // 회원가입 시, 유효성 체크
    public Map<String, String> validateHandling(Errors errors) {
        Map<String, String> validatorResult = new HashMap<>();

        for (FieldError error : errors.getFieldErrors()) {
            String validKeyName = "message";
            validatorResult.put(validKeyName, error.getDefaultMessage());
        }
        return validatorResult;
    }

    // 아이디 중복체크
    public boolean idDuplichk(String userName){
        return userRepository.findByUserName(userName).isPresent();
    }

    // 유저 정보 확인
    public IsLoginDto isloginCheck(UserDetailsImpl userDetails){
        String userName = userDetails.getUsername();
        String nickName = userDetails.getNickName();
        String profileImgUrl = userDetails.getProfileImgUrl();

        return IsLoginDto.builder()
                .userName(userName)
                .nickName(nickName)
                .profileImgUrl(profileImgUrl)
                .build();
    }

    // 페이징 처리
    public Page<MyLikePostDto> overPages(List<MyLikePostDto> postList, int start, int end, Pageable pageable, int pageno) {
        Page<MyLikePostDto> pages = new PageImpl(postList.subList(start, end), pageable, (long)postList.size());
        if (pageno > pages.getTotalPages()) {
            throw new IllegalArgumentException("요청할 수 없는 페이지 입니다.");
        } else {
            return pages;
        }
    }

    public Page<PostListDto> overPages2(List<PostListDto> postList, int start, int end, Pageable pageable, int pageno) {
        Page<PostListDto> pages = new PageImpl(postList.subList(start, end), pageable, (long)postList.size());
        if (pageno > pages.getTotalPages()) {
            throw new IllegalArgumentException("요청할 수 없는 페이지 입니다.");
        } else {
            return pages;
        }
    }

    public Page<ReviewListDto> overPages3(List<ReviewListDto> reviewList, int start, int end, Pageable pageable, int pageno) {
        Page<ReviewListDto> pages = new PageImpl<>(reviewList.subList(start, end), pageable, (long)reviewList.size());
        if (pageno > pages.getTotalPages()) {
            throw new IllegalArgumentException("요청할 수 없는 페이지입니다.");
        } else {
            return pages;
        }
    }
}