package com.hanghae.degether.doc.controller;

import com.hanghae.degether.doc.dto.FolderRequestDto;
import com.hanghae.degether.doc.dto.ResponseDto;
import com.hanghae.degether.doc.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class FolderController {
    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    // 폴더 등록
    @PostMapping("api/folder/{projectId}")
    public ResponseDto<?> addFolder(@RequestBody FolderRequestDto folderRequestDto,
                                    @PathVariable Long projectId) {
        return folderService.addFolder(folderRequestDto, projectId);
    }

    // 프로젝트에 등록한 모든 폴더 조회
    @GetMapping("api/folders/{projectId}")
    public ResponseDto<?> getFolders(@PathVariable Long projectId) {
        return folderService.getFolders(projectId);
    }

    // 폴더명 수정
    @PutMapping("api/folder/{folderId}")
    public ResponseDto<?> updateFolder(@PathVariable Long folderId,
                                       @RequestBody FolderRequestDto folderRequestDto) {
        return folderService.updateFolder(folderId, folderRequestDto);
    }

    @DeleteMapping("api/folder/{folderId}")
    public ResponseDto<?> deleteFolder(@PathVariable Long folderId) {
        return folderService.deleteFolder(folderId);
    }
}