package com.hanghae.degether.doc.dto;

import com.hanghae.degether.doc.model.Folder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FolderResponseDto {
    private Long id;
    private String folderName;

    public FolderResponseDto(Folder folder) {
        this.id = folder.getId();
        this.folderName = folder.getName();
    }
}
