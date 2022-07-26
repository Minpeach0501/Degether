package com.hanghae.degether.project.repository;

import com.hanghae.degether.project.model.Language;
import com.hanghae.degether.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LanguageRepository extends JpaRepository<Language,Long> {
    List<Language> findAllByUser(User user);
}
