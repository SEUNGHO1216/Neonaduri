package com.sparta.neonaduri_back.service;

/**
 * [Service] - 카카오 소셜 로그인 Service
 *
 * @class   : KakaoUserService
 * @author  : 오예령
 * @since   : 2022.04.30
 * @version : 1.0
 *
 *   수정일     수정자             수정내용
 *  --------   --------    ---------------------------
 *  2022.05.04 오예령       카카오 로그인 연결 성공
 *                         카카오 회원가입 시 profileImgUrl, email, totalLike 항목 추가
 *                         email 값 userName으로 변경
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sparta.neonaduri_back.dto.user.SocialLoginInfoDto;
import com.sparta.neonaduri_back.model.User;
import com.sparta.neonaduri_back.repository.UserRepository;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.security.jwt.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class KakaoUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;


    public SocialLoginInfoDto kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);
        System.out.println("카카오서비스에서 받은"+ code);
        System.out.println("1. 인가 코드로 액세스 토큰 요청");
        System.out.println("카카오서비스에서" + accessToken);

        // 2. 토큰으로 카카오 API 호출
        SocialLoginInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);
        System.out.println("2. 토큰으로 카카오 API 호출");

        // 3. 카카오ID로 회원가입 처리
        User kakaoUser = registerKakaoUserIfNeed(kakaoUserInfo);
        System.out.println("3. 카카오ID로 회원가입 처리");

        // 4. 강제 로그인 처리
        Authentication authentication = forceLogin(kakaoUser);
        System.out.println("4. 강제 로그인 처리");

        // 5. response Header에 JWT 토큰 추가
        kakaoUsersAuthorizationInput(authentication, response);

        System.out.println("5. response Header에 JWT 토큰 추가");

        return kakaoUserInfo;

    }

    // 1. "인가 코드"로 "액세스 토큰" 요청
    private String getAccessToken(String code) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        //        headers.add("code", code);

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "2e4e71fc3d3078adc996df889a6eb71a"); // 리액트
        body.add("redirect_uri", "http://localhost:3000/user/kakao/callback"); // 리액트
//        body.add("redirect_uri", "http://neonaduri.com/user/kakao/callback"); // 리액트
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    // 2. 토큰으로 카카오 API 호출
    private SocialLoginInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        String provider = "kakao";
        String kakaouserName = provider + "_" + jsonNode.get("id").asText(); // 로그인 아이디

        String userName = String.valueOf(jsonNode.get("kakao_account").get("email"));
        System.out.println("카카오 서비스에서 로그인할 때 받는 email" + userName);

        String nickName = jsonNode.get("properties")
                .get("nickname").asText();
        System.out.println("카카오 서비스에서 로그인할 때 받는 닉네임" + nickName);

        String profileImgUrl = String.valueOf(jsonNode.get("kakao_account").get("profile").get("profile_image_url"));

        System.out.println("카카오 토큰에 있는" + "" + kakaouserName + ""+ userName + "" + nickName +  "" + profileImgUrl);

        return new SocialLoginInfoDto(userName, nickName, profileImgUrl);

    }

    // 3. 카카오ID로 회원가입 처리
    private User registerKakaoUserIfNeed (SocialLoginInfoDto kakaoUserInfo) {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        String userName =kakaoUserInfo.getUserName();

        User kakaoUser = userRepository.findByUserName(userName)
                .orElse(null);

        if (kakaoUser == null) {
            // 회원가입
            // userName: kakao email

            String nickName = kakaoUserInfo.getNickName();

            Optional<User> nickNameCheck = userRepository.findByNickName(nickName);

            // 닉네임 중복 검사
            if (nickNameCheck.isPresent()) {
                String tempNickName = nickName;
                int i = 1;
                while (true){
                    nickName = tempNickName + "_" + i;
                    Optional<User> nickNameCheck2 = userRepository.findByNickName(nickName);
                    if (!nickNameCheck2.isPresent()) {
                        break;
                    }
                    i++;
                }
            }
            System.out.println("카카오 서비스에서 회원가입할 때 받는 닉네임" + nickName);

            // password: random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            String profileImgUrl = kakaoUserInfo.getProfileImgUrl();

            kakaoUser = new User(userName, nickName, encodedPassword, profileImgUrl);
            userRepository.save(kakaoUser);

        }
        return kakaoUser;
    }

    // 4. 강제 로그인 처리
    private Authentication forceLogin(User kakaoUser) {
        UserDetails userDetails = new UserDetailsImpl(kakaoUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    // 5. response Header에 JWT 토큰 추가
    private void kakaoUsersAuthorizationInput(Authentication authentication, HttpServletResponse response) {
        // response header에 token 추가
        UserDetailsImpl userDetailsImpl = ((UserDetailsImpl) authentication.getPrincipal());
        String token = JwtTokenUtils.generateJwtToken(userDetailsImpl);
        response.addHeader("Authorization", "BEARER" + " " + token);
    }
}