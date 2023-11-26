package com.disney.repository;

import com.disney.model.entity.Character;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CharacterRepositoryTest {
    private final CharacterRepository characterRepository;

    @Autowired
    public CharacterRepositoryTest(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    @DisplayName(value = "JUnit Test for check if Character name exists in database and result is true")
    @Test
    public void givenCharacterName_whenCheckIfExists_thenReturnTrue() {
        // given
        final String characterName = "Existent Character Name";
        final Character character = Character.builder()
                .image("character-image.jpg")
                .name(characterName)
                .age(31)
                .weight(93.7)
                .history("Character history")
                .movies(new HashSet<>(Collections.emptySet()))
                .build();
        characterRepository.save(character);

        // when
        boolean result = characterRepository.existsByName(characterName);

        //then
        assertThat(result).isTrue();
    }

    @DisplayName(value = "JUnit Test for check if Character name exists in database and result is false")
    @Test
    public void givenCharacterName_whenCheckIfExists_thenReturnFalse() {
        // given
        final String characterName = "other name";
        final Character character = Character.builder()
                .image("character-image.jpg")
                .name("Character Name")
                .age(31)
                .weight(93.7)
                .history("Character history")
                .movies(new HashSet<>(Collections.emptySet()))
                .build();
        characterRepository.save(character);

        // when
        boolean result = characterRepository.existsByName(characterName);

        //then
        assertThat(result).isFalse();
    }
}
