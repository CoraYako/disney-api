package com.disney.repository;

import com.disney.model.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, UUID> {

    boolean existsByName(String name);
}
