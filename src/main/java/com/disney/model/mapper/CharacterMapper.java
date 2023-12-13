package com.disney.model.mapper;

import com.disney.model.dto.request.CharacterRequestDto;
import com.disney.model.dto.response.CharacterResponseDto;
import com.disney.model.dto.response.basic.CharacterBasicResponseDto;
import com.disney.model.entity.Character;
import jakarta.validation.constraints.NotNull;

public interface CharacterMapper {

    Character toEntity(@NotNull CharacterRequestDto dto);

    CharacterResponseDto toDTO(@NotNull Character entity);

    CharacterBasicResponseDto toBasicDTO(@NotNull Character entity);
}
