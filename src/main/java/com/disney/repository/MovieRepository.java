package com.disney.repository;

import com.disney.model.entity.MovieEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<MovieEntity, Long> {

    @Query(value = "SELECT * FROM movies WHERE title LIKE :title", nativeQuery = true)
    Optional<MovieEntity> findByTitle(@Param("title") String title);

    List<MovieEntity> findAll(Specification<MovieEntity> specification);
}
