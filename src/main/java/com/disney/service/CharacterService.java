package com.disney.service;

import com.disney.model.dto.request.CharacterRequestDto;
import com.disney.model.dto.request.CharacterUpdateRequestDto;
import com.disney.model.dto.response.CharacterResponseDto;
import com.disney.model.entity.Character;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.util.Set;
import java.util.UUID;

public interface CharacterService {

    void createCharacter(@NotNull CharacterRequestDto requestDto);

    CharacterResponseDto updateCharacter(@NotNull String id, CharacterUpdateRequestDto requestDto);

    void deleteCharacter(@NotNull String id);

    Page<CharacterResponseDto> listCharacters(@NotNull int pageNumber, String characterName,
                                              int age, Set<String> moviesName);

    CharacterResponseDto getCharacterById(@NotNull String id);

    Character getCharacterById(@NotNull UUID id);
}
