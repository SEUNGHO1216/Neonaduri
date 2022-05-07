package com.sparta.neonaduri_back.service;

import com.sparta.neonaduri_back.dto.post.*;
import com.sparta.neonaduri_back.model.*;
import com.sparta.neonaduri_back.repository.DaysRepository;
import com.sparta.neonaduri_back.repository.LikeRepository;
import com.sparta.neonaduri_back.repository.PlacesRepository;
import com.sparta.neonaduri_back.repository.PostRepository;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.validator.UserInfoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final DaysRepository daysRepository;
    private final PlacesRepository placesRepository;
    private final LikeRepository likeRepository;
    private final UserInfoValidator validator;

    //방 만들기
    public RoomMakeRequestDto makeRoom(RoomMakeRequestDto roomMakeRequestDto, User user) {

        Post post= new Post(roomMakeRequestDto, user);
        postRepository.save(post);
        Long postId = post.getPostId();
        roomMakeRequestDto.setPostId(postId);
        return roomMakeRequestDto;
    }

    //자랑하기
    @Transactional
    public Long showAll(PostRequestDto postRequestDto, User user) {

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
                int likeCnt=countLike(post.getPostId());
                MyLikePostDto myLikePostDto=new MyLikePostDto(post.getPostId(), post.getPostImgUrl()
                ,post.getPostTitle(),post.getLocation(),post.getStartDate(),
                        post.getEndDate(),islike, likeCnt,post.getTheme());
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
    //게시물의 찜개수 세기
    private int countLike(Long postId) {
        return likeRepository.countByPostId(postId).intValue();
    }

    //totalLike 계산하기
    public int getTotalLike(UserDetailsImpl userDetails) {
        //----------------------totalLike 연산---------------------
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
    // 플랜 계획 조회하기
    public RoomMakeRequestDto getPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                ()-> new IllegalArgumentException("게시물이 존재하지 않습니다.")
        );
        RoomMakeRequestDto roomMakeRequestDto = new RoomMakeRequestDto(post.getPostId(), post.getStartDate(),
                post.getEndDate(), post.getDateCnt(), post.getPostTitle(), post.getLocation(), post.getTheme());
        return roomMakeRequestDto;
    }

    // 내가 작성한 플랜조회
    public Page<PostListDto> getMyPosts(int pageno, UserDetailsImpl userDetails) {

        // 유저가 작성한 글 조회
        List<Post> posts = postRepository.findAllByUser(userDetails.getUser());

        Pageable pageable = getPageable(pageno);

        List<PostListDto> myplanList = new ArrayList<>();

        for (Post post : posts) {
            PostListDto postListDto = new PostListDto(post.getPostId(), post.getStartDate(), post.getEndDate(), post.getDateCnt(), post.getPostTitle(),
                    post.getLocation(), post.getTheme());
            myplanList.add(postListDto);

        }

        int start = pageno * 6;
        int end = Math.min((start + 6), myplanList.size());

        return validator.overPages2(myplanList, start, end, pageable, pageno);
    }

    //삭제하기
    @Transactional
    public ResponseEntity<String> deletePlan(Long postId, UserDetailsImpl userDetails) {
        Post post = postRepository.findByPostId(postId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 게시물입니다.")
        );
        if (post == null) {
            return new ResponseEntity<>("없는 게시글입니다.", HttpStatus.BAD_REQUEST);
        }
        if (post.getUser().getUserName() != userDetails.getUser().getUserName()) {
            return new ResponseEntity<>("없는 사용자이거나 다른 사용자의 게시글입니다.", HttpStatus.BAD_REQUEST);
        }
        postRepository.deleteById(postId);
        return new ResponseEntity<>("성공적으로 삭제 하였습니다.",HttpStatus.OK);
    }

    //상세보기 - 수정 필요!
    public PostListDto detailPlan(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                ()-> new IllegalArgumentException("게시물이 존재하지 않습니다.")
        );
        return new PostListDto(post);
    }

    //플랜 저장 안함.(새로고침 뒤로가기)
    @Transactional
    public ResponseEntity<String> leavePost(Long postId, User user) {
        Post post = postRepository.findByPostId(postId).orElse(null);
        if (post == null) {
            return new ResponseEntity<>("없는 게시글입니다.", HttpStatus.BAD_REQUEST);
        }
        if (!Objects.equals(post.getUser().getUserName(), user.getUserName())) {
            return new ResponseEntity<>("없는 사용자이거나 다른 사용자의 게시글입니다.", HttpStatus.BAD_REQUEST);
        }
        postRepository.deleteById(postId);
        return new ResponseEntity<>("삭제 완료.",HttpStatus.OK);
    }
}
