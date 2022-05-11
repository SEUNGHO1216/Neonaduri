package com.sparta.neonaduri_back.service;

import com.sparta.neonaduri_back.dto.post.*;
import com.sparta.neonaduri_back.model.*;
import com.sparta.neonaduri_back.repository.*;
import com.sparta.neonaduri_back.security.UserDetailsImpl;
import com.sparta.neonaduri_back.utils.ImageBundle;
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
    private final ImageBundle imageBundle;

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
        postRequestDto.setPostImgUrl(imageBundle.searchImage());
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

        //리팩토링 필요
        for(Likes likes:likesList){

            Optional<Post> postOptional=postRepository.findById(likes.getPostId());

            //찜한 게시물이 존재할 경우
            if(postOptional.isPresent()){
                //찜한 게시물이니 true값 입력
                Post post=postOptional.get();
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
            if(post.getDays().size()==0) continue;
            //찜받은 갯수 확인
//            int likeCnt=countLike(post.getPostId());
            Long userId=userDetails.getUser().getId();
            //로그인 유저가 찜한 것인지 여부 확인
            post.setIslike(userLikeTrueOrNot(userId, post.getPostId()));
            //게시물의 reviewCnt 계산
            int reviewCnt=reviewRepository.countByPostId(post.getPostId()).intValue();

            BestAndLocationDto bestAndLocationDto =new BestAndLocationDto(post.getPostId(), post.getPostImgUrl(),post.getPostTitle(),
                    post.getLocation(),post.isIslike(), post.getLikeCnt(), reviewCnt);
            bestList.add(bestAndLocationDto);
        }
        return bestList;
    }

    //로그인한 유저가 찜한 게시물 인지 확인하는 메소드(setIsisLike)
    public boolean userLikeTrueOrNot(Long userId, Long postId){
        Optional<Likes> isUserLike=likeRepository.findByPostIdAndUserId(postId,userId);
        //유저가 찜한 기록이 있디면
        if(isUserLike.isPresent()){
            return true;
        }else{
            //찜한 기록 없다면
            return false;
        }
    }

    //지역별 검색(8개 조회)
    public Page<BestAndLocationDto> showLocationPosts(String location, int pageno, UserDetailsImpl userDetails) {

        List<Post> locationPostList=postRepository.findAllByLocationOrderByLikeCntDesc(location);

        List<BestAndLocationDto> locationList=new ArrayList<>();

        Pageable pageable= getPageableList(pageno);


        for(int i=0;i<locationPostList.size();i++){
            Post post=locationPostList.get(i);
            //나만보기 상태이면 추가 안함(jpa로 조건 걸 수 있으나 db에 너무 많은 작업이 가는 것 같아서 자바단에서 실행)
            if(!post.isIspublic() || post.getDays().size()==0) continue;
            //찜받은 갯수 확인
//            int likeCnt=countLike(post.getPostId());
            Long userId=userDetails.getUser().getId();
            //로그인 유저가 찜한 것인지 여부 확인
            post.setIslike(userLikeTrueOrNot(userId, post.getPostId()));
            //게시물의 reviewCnt 계산
            int reviewCnt=reviewRepository.countByPostId(post.getPostId()).intValue();

            BestAndLocationDto bestAndLocationDto =new BestAndLocationDto(post.getPostId(), post.getPostImgUrl(),post.getPostTitle(),
                    post.getLocation(),post.isIslike(), post.getLikeCnt(), reviewCnt);
            locationList.add(bestAndLocationDto);
        }

        int start=pageno*8;
        int end=Math.min((start+8), locationList.size());

        return validator.overPagesCheck(locationList,start,end,pageable,pageno);
    }
    //bestList, locationList 페이징
    private Pageable getPageableList(int pageno) {
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "id");
        return PageRequest.of(pageno, 8, sort);
    }

    //bestList, locationList 페이징
    private Pageable getPageableList5(int pageno) {
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "id");
        return PageRequest.of(pageno, 5, sort);
    }

    //테마별 검색조회(8개)
    public Page<ThemeAndSearchDto> showThemePosts(String theme, int pageno, UserDetailsImpl userDetails) {

        List<Post> themePostList=postRepository.findAllByThemeOrderByLikeCntDesc(theme);

        List<ThemeAndSearchDto> themeList=new ArrayList<>();

        Pageable pageable= getPageableList(pageno);


        for(int i=0;i<themePostList.size();i++){
            Post post=themePostList.get(i);

            //나만보기 상태이면 추가 안함(jpa로 조건 걸 수 있으나 db에 너무 많은 작업이 가는 것 같아서 자바단에서 실행)
            //추가로 days 길이가 0이면 방만 만들어지고, 여행계획은 세우지 않은 상황이니 마찬가지로 조회 시 걸러준다
            if(!post.isIspublic() || post.getDays().size()==0) continue;
            //찜받은 갯수 확인
//            int likeCnt=countLike(post.getPostId());
            Long userId=userDetails.getUser().getId();
            //로그인 유저가 찜한 것인지 여부 확인
            post.setIslike(userLikeTrueOrNot(userId, post.getPostId()));
            //게시물의 reviewCnt 계산
            int reviewCnt=reviewRepository.countByPostId(post.getPostId()).intValue();

            ThemeAndSearchDto themeAndSearchDto =new ThemeAndSearchDto(post.getPostId(), post.getPostImgUrl(),post.getPostTitle(),
                    post.getLocation(),post.isIslike(), post.getLikeCnt(), reviewCnt, post.getTheme());
            themeList.add(themeAndSearchDto);
        }

        int start=pageno*8;
        int end=Math.min((start+8), themeList.size());

        return validator.overPageCheck2(themeList,start,end,pageable,pageno);
    }

    //게시물 상세조회
    public Post showDetail(Long postId, UserDetailsImpl userDetails) {


        Post post=postRepository.findById(postId).orElseThrow(
                ()->new IllegalArgumentException("해당 게시물이 없습니다")
        );
        //전체공개이고
        if(post.isIspublic()){

            //조회수 증가 부분
            post.setViewCnt(post.getViewCnt()+1);
            postRepository.save(post);

            return postRepository.findById(postId).orElseThrow(
                    ()->new IllegalArgumentException("해당 게시물이 없습니다")
            );
        }else{
            //현재 유저가 작성자와 같으면
            if(post.getUser().getId() == userDetails.getUser().getId()){
                return postRepository.findById(postId).orElseThrow(
                        ()->new IllegalArgumentException("해당 게시물이 없습니다")
                );
            }else{
                return null;
            }
        }
    }

    //여행 게시물 삭제
    @Transactional
    public Long deletePost(UserDetailsImpl userDetails, Long postId) {
        Post post=postRepository.findById(postId).orElseThrow(
                ()->new IllegalArgumentException("해당 게시물이 없으므로 삭제할 수 없습니다")
        );
        if(post.getUser().getId()!=userDetails.getUser().getId()){
            throw new IllegalArgumentException("게시물 작성자만 삭제가 가능합니다");
        }
        reviewRepository.deleteAllByPostId(postId);
        likeRepository.deleteAllByPostId(postId);
        postRepository.deleteById(postId);

        return postId;
    }



    //검색결과 조회
    public Page<ThemeAndSearchDto> showSearchPosts(int pageno, String keyword, UserDetailsImpl userDetails) {
        String postTitle=keyword;
        String location=keyword;
        String theme=keyword;

        List<Post> postList=postRepository.findByPostTitleContainingOrLocationContainingOrThemeContainingOrderByModifiedAtDesc(
                postTitle,location,theme
        );
        List<ThemeAndSearchDto> searchList=new ArrayList<>();

        Pageable pageable= getPageableList(pageno);


        for(int i=0;i<postList.size();i++){
            Post post=postList.get(i);
            //나만보기 상태이면 추가 안함(jpa로 조건 걸 수 있으나 db에 너무 많은 작업이 가는 것 같아서 자바단에서 실행)
            if(!post.isIspublic() || post.getDays().size()==0) continue;
            //찜받은 갯수 확인
//            int likeCnt=countLike(post.getPostId());
            Long userId=userDetails.getUser().getId();
            //로그인 유저가 찜한 것인지 여부 확인
            post.setIslike(userLikeTrueOrNot(userId, post.getPostId()));
            //게시물의 reviewCnt 계산
            int reviewCnt=reviewRepository.countByPostId(post.getPostId()).intValue();

            ThemeAndSearchDto themeAndSearchDto =new ThemeAndSearchDto(post.getPostId(), post.getPostImgUrl(),post.getPostTitle(),
                    post.getLocation(),post.isIslike(), post.getLikeCnt(), reviewCnt, post.getTheme());
            searchList.add(themeAndSearchDto);
        }

        int start=pageno*8;
        int end=Math.min((start+8), searchList.size());

        return validator.overPageCheck2(searchList,start,end,pageable,pageno);
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
//--------------------------------------------------------------------------------------
    // 내가 작성한 플랜조회
    public Page<PostListDto> getMyPosts(int pageno, UserDetailsImpl userDetails) {

        // 유저가 작성한 글 조회
        List<Post> posts = postRepository.findAllByUser(userDetails.getUser());

        Pageable pageable = getPageableList5(pageno);

        List<PostListDto> myplanList = new ArrayList<>();

        Long userId=userDetails.getUser().getId();

        for (Post post : posts) {

            post.setIslike(userLikeTrueOrNot(userId,post.getPostId()));
            int reviewCnt=reviewRepository.countByPostId(post.getPostId()).intValue();
            if(post.getDays().size()==0) continue;
            PostListDto postListDto = new PostListDto(post.getPostId(), post.getPostImgUrl(),
                    post.getStartDate(), post.getEndDate(), post.getPostTitle(),
                    post.getLocation(), post.getTheme(),post.isIslike(), post.isIspublic(),
                    post.getLikeCnt(), reviewCnt);
            myplanList.add(postListDto);

        }
        int start = pageno * 5;
        int end = Math.min((start + 5), myplanList.size());

        return validator.overPages2(myplanList, start, end, pageable, pageno);
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
