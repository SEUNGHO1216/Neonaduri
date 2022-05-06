package com.sparta.neonaduri_back.service;

/**
 * [Service] - 구글 소셜 로그인 Service
 *
 * @class   : GoogleLoginService
 * @author  : 오예령
 * @since   : 2022.04.30
 * @version : 1.0
 *
 *   수정일     수정자             수정내용
 *  --------   --------    ---------------------------
 *  2022.05.04 오예령       email 값 userName으로 변경, 유저 정보 조회 항목 변경
 *                         유저 정보 조회 Url 변경
 *  2022.05.06 오예령       JwtProperties 클래스 삭제 및 header에 토큰 넣는 코드 수정
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class GoogleLoginService {

//    @Value("${google.client-id}")
//    String googleClientId;
//
//    @Value("${google.client-secret}")
//    String googleClientSecret;

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // 구글 로그인
    public void googleLogin(String code, HttpServletResponse response) throws JsonProcessingException {

        // 1. 인가코드로 엑세스토큰 가져오기
        String accessToken = getAccessToken(code);
        System.out.println("액세스토큰" + accessToken);

        // 2. 엑세스토큰으로 유저정보 가져오기
        SocialLoginInfoDto googleUserInfo = getGoogleUserInfo(accessToken);

        // 3. 유저확인 & 회원가입
        User foundUser = getUser(googleUserInfo);

        // 4. 시큐리티 강제 로그인
        Authentication authentication = securityLogin(foundUser);

        // 5. jwt 토큰 발급
        jwtToken(response, authentication);
    }

    // 1. 인가코드로 엑세스토큰 가져오기
    private String getAccessToken(String code) throws JsonProcessingException {

        // 헤더에 Content-type 지정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 바디에 필요한 정보 담기
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id" , ""); // 리액트
        body.add("client_secret", "");  // 리액트
        body.add("code", code);
        body.add("redirect_uri", "http://localhost:3000/user/google/callback");
        body.add("grant_type", "authorization_code");

        // POST 요청 보내기
        HttpEntity<MultiValueMap<String, String>> googleToken = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST, googleToken,
                String.class
        );

        // response에서 엑세스토큰 가져오기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseToken = objectMapper.readTree(responseBody);
        return responseToken.get("access_token").asText();
    }

    // 2. 엑세스토큰으로 유저정보 가져오기
    private SocialLoginInfoDto getGoogleUserInfo(String accessToken) throws JsonProcessingException {

        // 헤더에 엑세스토큰 담기, Content-type 지정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "BEARER" + " " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // POST 요청 보내기
        HttpEntity<MultiValueMap<String, String>> googleUser = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://openidconnect.googleapis.com/v1/userinfo",
                HttpMethod.POST, googleUser,
                String.class
        );

        // response에서 유저정보 가져오기
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode googleUserInfo = objectMapper.readTree(responseBody);

        String userName = googleUserInfo.get("email").asText();
        String nickName = googleUserInfo.get("name").asText();
        String profileImgUrl = googleUserInfo.get("picture").asText();

        return new SocialLoginInfoDto(userName,nickName,profileImgUrl);
    }

    // 3. 유저확인 & 회원가입
    private User getUser(SocialLoginInfoDto googleUserInfo) {

        String userName = googleUserInfo.getUserName();
        String nickName = googleUserInfo.getNickName();

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
        String password = passwordEncoder.encode(UUID.randomUUID().toString());
        String profileImgUrl = googleUserInfo.getProfileImgUrl();

        int totalLike = 0;

        // DB에서 userName으로 가져오기 없으면 회원가입
        User findUser = userRepository.findByUserName(userName).orElse(null);
        if (findUser == null) {
            findUser = User.builder()
                    .userName(userName)
                    .nickName(nickName)
                    .password(password)
                    .profileImgUrl(profileImgUrl)
                    .totalLike(totalLike)
                    .build();
            System.out.println("구글 서비스에서 회원가입할 때 보내는" + "userName " + userName + "nickName " + nickName + profileImgUrl + totalLike);
            userRepository.save(findUser);
        }
        return findUser;
    }

    // 4. 시큐리티 강제 로그인
    private Authentication securityLogin(User findUser) {

        // userDetails 생성
        UserDetailsImpl userDetails = new UserDetailsImpl(findUser);
        log.info("google 로그인 완료 : " + userDetails.getUser().getUserName());
        // UsernamePasswordAuthenticationToken 발급
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        // 강제로 시큐리티 세션에 접근하여 authentication 객체를 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    // 5. jwt 토큰 발급
    private void jwtToken(HttpServletResponse response, Authentication authentication) {

//        String token = JWT.create()
//                // 토큰이름
//                .withSubject(userDetails.getUser().getUserName())
//                // 유효시간
//                .withClaim("expireDate", new Date(System.currentTimeMillis() + JwtProperties.tokenValidTime))
//                // userName
//                .withClaim("userName", userDetails.getUser().getUserName())
//                // nickName
//                .withClaim("nickName", userDetails.getUser().getNickName())
//                // email
//                .withClaim("email", userDetails.getUser().getEmail())
//                // profileImgUrl
//                .withClaim("profileImgUrl", userDetails.getUser().getProfileImgUrl())
//                // totalLike
//                .withClaim("totalLike", userDetails.getUser().getTotalLike())
//                // HMAC256 복호화
//                .sign(Algorithm.HMAC256(JwtProperties.secretKey));

//        System.out.println("구글 서비스에서 로그인 후 토큰에서 보는" + "userName" + authentication.getPrincipal() + "nickName" + userDetails.getUser().getNickName() + userDetails.getUser().getProfileImgUrl() + userDetails.getUser().getTotalLike());

//        log.info("jwtToken : " + token);
//        response.addHeader("Authorization", "BEARER" + " " + token);
//        System.out.println(token);

        UserDetailsImpl userDetailsImpl = ((UserDetailsImpl) authentication.getPrincipal());
        String token = JwtTokenUtils.generateJwtToken(userDetailsImpl);
        response.addHeader("Authorization", "BEARER" + " " + token);

    }
}