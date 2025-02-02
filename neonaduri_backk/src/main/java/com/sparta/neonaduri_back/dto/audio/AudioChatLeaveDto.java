package com.sparta.neonaduri_back.dto.audio;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AudioChatLeaveDto {
    private Long postId;
    private String memberName;
    private String role;
    private String token;
}

