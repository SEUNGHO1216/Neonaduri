package com.sparta.neonaduri_back.validator;


import com.sparta.neonaduri_back.dto.user.IsLoginDto;
import com.sparta.neonaduri_back.dto.user.SignupRequestDto;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.repository.UserRepository;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
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

    //로그인 확인
    public IsLoginDto isloginCheck(UserDetailsImpl userDetails){
        System.out.println(userDetails.getUser().getUserName());
        String userName = userDetails.getUsername();
        String nickName = userDetails.getNickName();
        String profileImgUrl = userDetails.getProfileImgUrl();
        int totalLike = userDetails.getTotalLike();

//        Optional<User> user = userRepository.findByUserName(userName);
        return IsLoginDto.builder()
                .userName(userName)
                .nickName(nickName)
                .profileImgUrl(profileImgUrl)
                .totalLike(totalLike)
                .build();
    }

//    public Page<PostListDto> overPages(List<PostListDto> postList, int start, int end, Pageable pageable, int page) {
//        Page<PostListDto> pages = new PageImpl<>(postList.subList(start, end), pageable, postList.size());
//        if(page > pages.getTotalPages()){
//            throw new IllegalArgumentException("요청할 수 없는 페이지 입니다.");
//        }
//        return pages;
//    }


}