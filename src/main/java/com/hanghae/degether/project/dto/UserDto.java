package com.hanghae.degether.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String profileUrl;
    private String role;
    private String nickname;
}
