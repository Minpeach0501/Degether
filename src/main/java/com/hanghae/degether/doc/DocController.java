package com.hanghae.degether.doc;


import com.hanghae.degether.user.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class DocController {
    private final DocService docService;

    @Autowired
    public DocController(DocService docService){
        this.docService = docService;
    }

    @PostMapping("/api/doc/{projectId}")
    public ResponseDto<Object> createDoc(@PathVariable Long projectId,
                                  @RequestBody DocRequestDto docRequestDto,
                                  @AuthenticationPrincipal UserDetailsImpl userDetails){
        return docService.createDoc(projectId, docRequestDto, userDetails);
    }

    @GetMapping("/api/docs/{projectId}")
    public ResponseDto<?> getDocs(@PathVariable Long projectId){ return docService.getDocs(projectId); }

    @GetMapping("/api/doc/{docId}")
    public ResponseDto<Object> getDoc(@PathVariable Long docId){ return docService.getDoc(docId); }

    @PutMapping("/api/doc/{docId}")
    public ResponseDto<Object> updateDoc(@PathVariable Long docId,
                                         @RequestBody DocRequestDto docRequestDto,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return docService.updateDoc(docId, docRequestDto, userDetails);
    }

    @DeleteMapping("/api/doc/{docId}")
    public ResponseDto<Object> deleteDoc(@PathVariable Long docId,
                                         @AuthenticationPrincipal UserDetailsImpl userDetails){
        return docService.deleteDoc(docId, userDetails);
    }

    @GetMapping("/api/onGoing/{projectId}")
    public ResponseDto<?> onGoing(@PathVariable Long projectId){
        return docService.onGoing(projectId);
    }

    @PutMapping("/api/docStatus/{docId}")
    public ResponseDto<Object> docStatus(@PathVariable Long docId,
                                         @RequestBody StatusDto statusDto,
                                         UserDetailsImpl userDetails){
        return docService.docStatus(docId,statusDto, userDetails);
    }
}
