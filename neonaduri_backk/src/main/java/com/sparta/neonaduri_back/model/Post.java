package com.sparta.neonaduri_back.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sparta.neonaduri_back.dto.post.PostRequestDto;
import com.sparta.neonaduri_back.dto.post.RoomMakeRequestDto;
import com.sparta.neonaduri_back.utils.ImageBundle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Post extends Timestamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false)
    private String startDate;

    @Column(nullable = false)
    private String endDate;

    @Column(nullable = false)
    private int dateCnt;

    @Column(nullable = false)
    private String postTitle;

    @Column(nullable = false)
    private String location;

    @Column(nullable = true,length = 500)
    private String postImgUrl;

    @Column(nullable = false)
    private String theme;

    //굳이 필요한 것인가?
    @Column(nullable = true)
    private boolean islike;

    @Column(nullable = true)
    private int likeCnt;

    @Column(nullable = true)
    private int viewCnt;

    @Column(nullable = true)
    private boolean ispublic;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "days")
    private List<Days> days = new ArrayList<>();

    //방 만들어줄 때 생성자
    public Post(RoomMakeRequestDto roomMakeRequestDto, User user){
        this.startDate=roomMakeRequestDto.getStartDate();
        this.endDate=roomMakeRequestDto.getEndDate();
        this.dateCnt=roomMakeRequestDto.getDateCnt();
        this.postTitle=roomMakeRequestDto.getPostTitle();
        this.location=roomMakeRequestDto.getLocation();
        this.theme=roomMakeRequestDto.getTheme();
        this.user=user;
    }

    //저장할때 추가로 필요한 post정보
    public void completeSave(PostRequestDto postRequestDto,List<Days> daysList){
        this.postImgUrl=postRequestDto.getPostImgUrl();
        this.ispublic=postRequestDto.isIspublic();
        this.days=daysList;
    }

    //likeCnt 정보 수정
    public void updateLikeCnt(int likeCnt){
        this.likeCnt=likeCnt;
    }


}

