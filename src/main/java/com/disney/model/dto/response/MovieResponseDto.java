package com.disney.model.dto.response;

import com.disney.model.dto.response.basic.CharacterBasicResponseDto;
import com.disney.model.dto.response.basic.GenreBasicResponseDto;

import java.util.Set;

public record MovieResponseDto(
        String id,
        String image,
        String title,
        String creationDate,
        int rate,
        GenreBasicResponseDto genre,
        Set<CharacterBasicResponseDto> characters
) {
}
