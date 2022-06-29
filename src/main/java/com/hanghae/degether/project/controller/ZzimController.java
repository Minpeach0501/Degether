package com.hanghae.degether.project.controller;

import com.hanghae.degether.project.dto.ResponseDto;
import com.hanghae.degether.project.service.ZzimService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ZzimController {
    private final ZzimService zzimService;


}
