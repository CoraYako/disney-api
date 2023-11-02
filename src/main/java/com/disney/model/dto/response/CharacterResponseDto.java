package com.disney.model.dto.response;

import com.disney.model.dto.response.basic.MovieBasicInfoResponseDto;

import java.util.Set;

public record CharacterResponseDto(
        Long id,
        String image,
        String name,
        int age,
        double weight,
        String history,
        Set<MovieBasicInfoResponseDto> movies
) {
}
