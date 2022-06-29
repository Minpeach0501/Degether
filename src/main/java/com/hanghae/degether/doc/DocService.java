package com.hanghae.degether.doc;

import com.hanghae.degether.User.User;
import com.hanghae.degether.User.UserRepository;
import com.hanghae.degether.User.security.UserDetailsImpl;
import com.hanghae.degether.project.Project;
import com.hanghae.degether.project.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DocService {

    private final DocRepository docRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Autowired
    public DocService(DocRepository docRepository, ProjectRepository projectRepository, UserRepository userRepository){
        this.docRepository = docRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResponseDto<Object> createDoc(Long projectId, DocRequestDto docRequestDto, UserDetailsImpl userDetails) {
        Project project = projectRepository.findById(projectId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        User user = userDetails.getUser();
        User user1 = userRepository.findById(docRequestDto.getInCharge()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 유저입니다."));
        Doc doc = new Doc(project, docRequestDto, user, user1);
        docRepository.save(doc);
        return new ResponseDto<>(true, "작성성공");
    }
    @Transactional
    public ResponseDto<?> getDocs(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        List<DocResponseDto> docs = new ArrayList<>();
        for(Doc doc : docRepository.findAllByProjectOrderByCreatedDateDesc(project)){
            docs.add(new DocResponseDto(doc.getId(), doc.getTitle(), doc.getUser().getNickname()));
        }
        return new ResponseDto<List<DocResponseDto>>(true,"요청성공",docs);
    }

    @Transactional
    public ResponseDto<Object> getDoc(Long docId) {
        Doc doc = docRepository.findById(docId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 문서입니다."));
        DocResponseDto docResponseDto = new DocResponseDto(doc);
        return  new ResponseDto<>(true,"요청성공", docResponseDto);
    }


    @Transactional
    public ResponseDto<Object> updateDoc(Long docId, DocRequestDto docRequestDto, UserDetailsImpl userDetails) {
        Doc doc = docRepository.findById(docId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 문서입니다."));
        if(userDetails.getUser().getId() != doc.getId()){
            return new ResponseDto<>(false,"작성자만 수정할 수 있습니다.");
        }
        User user = userRepository.findById(docRequestDto.getInCharge()).orElseThrow(()->new IllegalArgumentException("존재하지 않는 유저입니다."));
        doc.update(docRequestDto, user);
        return  new ResponseDto<>(true,"수정 성공.");
    }

    @Transactional
    public ResponseDto<Object> deleteDoc(Long docId, UserDetailsImpl userDetails) {
        Doc doc = docRepository.findById(docId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 문서입니다."));
        if(userDetails.getUser().getId() != doc.getId()){
            return new ResponseDto<>(false,"작성자만 삭제할 수 있습니다.");
        }
        docRepository.deleteById(docId);
        return new ResponseDto<>(true,"삭제 성공.");
    }

    @Transactional
    public ResponseDto<?> onGoing(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 프로젝트입니다."));
        List<DocResponseDto> docs = new ArrayList<>();
        for(Doc doc : docRepository.findAllByProjectOrderByCreatedDateDesc(project)){
            docs.add(new DocResponseDto(doc.getId(), doc.getTitle(), doc.getUser().getNickname()));
        }
        return new ResponseDto<List<DocResponseDto>>(true,"요청성공",docs);
    }

    @Transactional
    public ResponseDto<Object> docStatus(Long docId, StatusDto statusDto, UserDetailsImpl userDetails) {
        Doc doc = docRepository.findById(docId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 문서입니다."));
        if(userDetails.getUser().getId() != doc.getUser().getId()){
            return new ResponseDto<>(false,"작성자만 수정 가능합니다.");
        }
        doc.update(statusDto);
        return new ResponseDto<>(true, "수정 완료");
    }
}
