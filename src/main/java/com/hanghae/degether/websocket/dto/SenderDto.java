package com.hanghae.degether.websocket.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderDto {
    private Long id;
    private String profileUrl;
    private String role;
    private String nickname;
}
