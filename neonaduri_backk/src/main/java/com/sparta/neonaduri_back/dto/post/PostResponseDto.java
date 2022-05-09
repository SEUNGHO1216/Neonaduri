package com.sparta.neonaduri_back.dto.post;

import com.sparta.neonaduri_back.model.Post;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
public class PostResponseDto {
    private List<PostListDto> myplanList;
    private int totalPage;
    private boolean islastPage;

//    public PostResponseDto(Post post) {
//        this.postId = post.getPostId();
//        this.startDate = post.getStartDate();
//        this.endDate = post.getEndDate();
//        this.dateCnt = post.getDateCnt();
//        this.postTitle = post.getPostTitle();
//        this.location = post.getLocation();
//        this.theme = post.getTheme();
//    }

    public PostResponseDto(Page<PostListDto> getMyPosts, boolean islastPage) {
        this.myplanList = getMyPosts.getContent();
        this.totalPage = getMyPosts.getTotalPages();
        this.islastPage = islastPage;

    }
}