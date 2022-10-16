package com.disney.repository;

import com.disney.model.entity.GenreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Long> {

    @Query(value = "SELECT * FROM genres WHERE name LIKE :name", nativeQuery = true)
    Optional<GenreEntity> findByName(@Param("name") String name);
}
