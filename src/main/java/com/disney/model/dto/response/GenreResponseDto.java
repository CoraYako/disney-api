package com.disney.model.dto.response;

import com.disney.model.dto.response.basic.MovieBasicResponseDto;

import java.util.Set;

public record GenreResponseDto(
        String id,
        String name,
        Set<MovieBasicResponseDto> movies
) {
}
