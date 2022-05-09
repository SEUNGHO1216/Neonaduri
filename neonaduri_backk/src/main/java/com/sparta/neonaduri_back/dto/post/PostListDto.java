package com.sparta.neonaduri_back.dto.post;

import com.sparta.neonaduri_back.model.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostListDto {
    private Long postId;
    private String startDate;
    private String endDate;
    private int dateCnt;
    private String postTitle;
    private String location;
    private String theme;

    public PostListDto(Post post) {
        this.postId = post.getPostId();
        this.startDate = post.getStartDate();
        this.endDate = post.getEndDate();
        this.dateCnt = post.getDateCnt();
        this.postTitle = post.getPostTitle();
        this.location = post.getLocation();
        this.theme = post.getTheme();
    }
}
