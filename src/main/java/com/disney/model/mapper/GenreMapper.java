package com.disney.model.mapper;

import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.model.dto.response.basic.GenreBasicResponseDto;
import com.disney.model.entity.Genre;
import jakarta.validation.constraints.NotNull;

public interface GenreMapper {

    Genre toEntity(@NotNull GenreRequestDto dto);

    GenreResponseDto toDTO(@NotNull Genre entity);

    GenreBasicResponseDto toBasicDTO(@NotNull Genre entity);
}
