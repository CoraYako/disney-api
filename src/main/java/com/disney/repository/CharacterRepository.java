package com.disney.repository;

import com.disney.model.entity.CharacterEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<CharacterEntity, Long> {

    @Query(value = "SELECT * FROM characters WHERE name LIKE :name", nativeQuery = true)
    Optional<CharacterEntity> findByName(@Param("name") String name);

    List<CharacterEntity> findAll(Specification<CharacterEntity> specification);
}
