package com.hanghae.degether.project.repository;

import com.hanghae.degether.project.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
