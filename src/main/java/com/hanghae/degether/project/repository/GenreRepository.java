package com.hanghae.degether.project.repository;

import com.hanghae.degether.project.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {
}
