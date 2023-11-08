package com.disney.repository;

import com.disney.model.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CharacterRepository extends JpaRepository<Character, UUID>, JpaSpecificationExecutor<Character> {

    boolean existsByName(String name);
}
