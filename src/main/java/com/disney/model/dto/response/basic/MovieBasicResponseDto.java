package com.disney.model.dto.response.basic;

import java.util.Set;

public record MovieBasicResponseDto(
        Long id,
        String image,
        String title,
        String creationDate,
        int rate,
        Set<CharacterBasicResponseDto> characters
) {
}
