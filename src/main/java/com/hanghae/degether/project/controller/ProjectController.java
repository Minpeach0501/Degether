package com.hanghae.degether.project.controller;

import com.hanghae.degether.project.dto.ProjectRequestDto;
import com.hanghae.degether.project.dto.ProjectResponseDto;
import com.hanghae.degether.project.dto.ResponseDto;
import com.hanghae.degether.project.service.ProjectService;
import com.hanghae.degether.user.model.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProjectController {
    private final ProjectService projectService;

    // 프로젝트 생성
    @PostMapping("/project")
    public ResponseDto<?> createProject(
            @RequestPart ProjectRequestDto.Create projectRequestDto,
            @RequestPart(value = "thumbnail") MultipartFile multipartFile) {
        return ResponseDto.builder()
                .ok(true)
                .message("생성 성공")
                .projectId(projectService.createProject(projectRequestDto, multipartFile))
                .build();
    }
    @PutMapping("/project/{projectId}")
    public ResponseDto<?> modifyProject(
            @PathVariable Long projectId,
            @RequestPart ProjectRequestDto.Create projectRequestDto,
            @RequestPart(value = "thumbnail") MultipartFile multipartFile) {
        return ResponseDto.builder()
                .ok(true)
                .message("수정 성공")
                .result(projectService.modifyProject(projectId, projectRequestDto, multipartFile))
                .build();
    }

    // 프로젝트 리스트
    @GetMapping("/projects")
    public ResponseDto<?> getProjects(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "step", required = false) String step) {
        return ResponseDto.builder()
                .ok(true)
                .message("요청 성공")
                .results(projectService.getProject(search, language, genre, step))
                .build();
    }

    //찜하기, 찜삭제
    @PostMapping("/projectZzim/{projectId}")
    public ResponseDto<?> projectZzim(@PathVariable Long projectId) {
        projectService.projectZzim(projectId)
        return ResponseDto.builder()
                .ok(true)
                .message("요청 성공")
                .build();
    }

}
