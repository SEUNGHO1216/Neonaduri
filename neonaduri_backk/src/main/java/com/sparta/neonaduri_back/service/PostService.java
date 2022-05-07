package com.sparta.neonaduri_back.service;

import com.sparta.neonaduri_back.dto.post.*;
import com.sparta.neonaduri_back.model.*;
import com.sparta.neonaduri_back.repository.*;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.validator.UserInfoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final DaysRepository daysRepository;
    private final PlacesRepository placesRepository;
    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;
    private final UserInfoValidator validator;

    //방 만들기
    public RoomMakeRequestDto makeRoom(RoomMakeRequestDto roomMakeRequestDto, User user) {

        Post post= new Post(roomMakeRequestDto, user);
        postRepository.save(post);
        Long postId=post.getPostId();
        roomMakeRequestDto.setPostId(postId);
        return roomMakeRequestDto;
    }

    //자랑하기
    @Transactional
    public Long showAll(PostRequestDto postRequestDto, User user) {

        postRepository.findByUserAndPostId(user, postRequestDto.getPostId()).orElseThrow(
                ()->new IllegalArgumentException("방을 생성한 유저만 여행 계획 저장이 가능합니다.")
        );

        List<DayRequestDto> dayRequestDtoList= postRequestDto.getDays();
        List<Days> daysList=new ArrayList<>();
        for(int i=0; i<dayRequestDtoList.size();i++){
            //n일차 구하기
            int dateNumber=i+1;
            System.out.println("일차"+dateNumber);
            List<PlaceRequestDto> placeRequestDtoList=dayRequestDtoList.get(i).getPlaces();
            List<Places> placesList=new ArrayList<>();
            //n일차에 대한 n개의 방문 장소 Places entity에 저장
            for(PlaceRequestDto placeRequestDtos:placeRequestDtoList){
                Places places= new Places(placeRequestDtos);
                placesRepository.save(places);
                placesList.add(places);
            }
            //Days entity에 저장
            Days days= new Days(dateNumber, placesList);
            daysRepository.save(days);
            daysList.add(days);
        }
        Post post=postRepository.findById(postRequestDto.getPostId()).orElseThrow(
                ()->new NullPointerException("해당 계획이 없습니다")
        );
        //전체 여행계획 저장
        post.completeSave(postRequestDto,daysList);
        postRepository.save(post);
        return post.getPostId();
    }

    //내가 찜한 게시물 조회
    public Page<MyLikePostDto> showMyLike(int pageno, UserDetailsImpl userDetails) {

        //찜한 게시물 리스트
        List<MyLikePostDto> postList=new ArrayList<>();
        //찜 엔티티에서 자신의 id를 통해 찾으면 자기가 찜한 게시물이 뜰것! (최근 찜한거 부터 가져오기)
        List<Likes> likesList=likeRepository.findAllByUserIdOrderByModifiedAtDesc(userDetails.getUser().getId());
        Pageable pageable= getPageable(pageno);

        if(likesList.size()==0){
            throw new IllegalArgumentException("찜한 게시물이 없습니다");
        }else{
            //리팩토링 필요
            for(Likes likes:likesList){
                Post post=postRepository.findById(likes.getPostId()).orElseThrow(
                        ()-> new IllegalArgumentException("해당 게시물이 없습니다")
                );
                //찜한 게시물이니 true값 입력
                boolean islike=true;
//                int likeCnt=countLike(post.getPostId());
                MyLikePostDto myLikePostDto=new MyLikePostDto(post.getPostId(), post.getPostImgUrl()
                ,post.getPostTitle(),post.getLocation(),post.getStartDate(),
                        post.getEndDate(),islike, post.getLikeCnt(),post.getTheme());
                postList.add(myLikePostDto);
            }
        }
        int start = pageno * 6;
        int end = Math.min((start + 6), postList.size());

        return validator.overPages(postList, start, end, pageable, pageno);
    }

    //페이징
    private Pageable getPageable(int pageno) {
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "id");
        return PageRequest.of(pageno, 6, sort);
    }
//    //게시물의 찜개수 세기 (likeCnt)
//    private int countLike(Long postId) {
//        return likeRepository.countByPostId(postId).intValue();
//    }

    //totalLike 계산하기
    public int getTotalLike(UserDetailsImpl userDetails) {

        //내가 쓴 게시물 다 조회
        List<Post> posts=postRepository.findAllByUserOrderByModifiedAtDesc(userDetails.getUser());
        int totalLike=0;

        //내가 쓴 게시물이 있다면 찜 엔티티에서 게시물 갯수 카운트 -> 유저들한테 찜받은 갯수를 말함
        for(Post eachPost: posts){
            Long postId=eachPost.getPostId();
            totalLike+=likeRepository.countByPostId(postId);
        }
        return totalLike;
    }

    //BEST 4 게시물 조회
    public List<BestAndLocationDto> showBestPosts(UserDetailsImpl userDetails) {

        List<Post> postList=postRepository.findAllByIspublicTrueOrderByLikeCntDesc();
        List<BestAndLocationDto> bestList=new ArrayList<>();

        for(int i=0;i<postList.size();i++){
            if(i>3) break;
            Post post=postList.get(i);
            //찜받은 갯수 확인
//            int likeCnt=countLike(post.getPostId());
            Long userId=userDetails.getUser().getId();
            //로그인 유저가 찜한 것인지 여부 확인
            post.setIslike(userLikeTrueOrNot(userId, post.getPostId()));
            //게시물의 reviewCnt 계산
            int reviewCnt=reviewRepository.countByPost(post).intValue();

            BestAndLocationDto bestAndLocationDto =new BestAndLocationDto(post.getPostId(), post.getPostImgUrl(),post.getPostTitle(),
                    post.getLocation(),post.isIslike(), post.getLikeCnt(), reviewCnt);
            bestList.add(bestAndLocationDto);
        }
        return bestList;
    }

    //로그인한 유저가 찜한 게시물 인지 확인하는 메소드(setIsisLike)
    public boolean userLikeTrueOrNot(Long userId, Long postId){
        Optional<Likes> isUserLike=likeRepository.findByPostIdAndUserId(postId,userId);
        if(isUserLike.isPresent()){
            return false;
        }else{
            return true;
        }
    }

    //지역별 검색(20개 조회)
    public List<BestAndLocationDto> showLocationPosts(String location, UserDetailsImpl userDetails) {
        List<Post> locationPostList=postRepository.findAllByLocationOrderByLikeCntDesc(location);

        List<BestAndLocationDto> locationList=new ArrayList<>();

        for(int i=0;i<locationPostList.size();i++){
            if(i>19) break;
            Post post=locationPostList.get(i);
            //나만보기 상태이면 추가 안함(jpa로 조건 걸 수 있으나 db에 너무 많은 작업이 가는 것 같아서 자바단에서 실행)
            if(!post.isIspublic()) continue;
            //찜받은 갯수 확인
//            int likeCnt=countLike(post.getPostId());
            Long userId=userDetails.getUser().getId();
            //로그인 유저가 찜한 것인지 여부 확인
            post.setIslike(userLikeTrueOrNot(userId, post.getPostId()));
            //게시물의 reviewCnt 계산
            int reviewCnt=reviewRepository.countByPost(post).intValue();

            BestAndLocationDto bestAndLocationDto =new BestAndLocationDto(post.getPostId(), post.getPostImgUrl(),post.getPostTitle(),
                    post.getLocation(),post.isIslike(), post.getLikeCnt(), reviewCnt);
            locationList.add(bestAndLocationDto);
        }
        return locationList;
    }
}

