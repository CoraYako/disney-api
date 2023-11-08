package com.disney.model.mapper;

import com.disney.model.dto.request.MovieRequestDto;
import com.disney.model.dto.response.MovieResponseDto;
import com.disney.model.dto.response.basic.MovieBasicInfoResponseDto;
import com.disney.model.dto.response.basic.MovieBasicResponseDto;
import com.disney.model.entity.Movie;
import jakarta.validation.constraints.NotNull;

public interface MovieMapper {

    Movie toEntity(@NotNull MovieRequestDto dto);

    MovieResponseDto toDTO(@NotNull Movie entity);

    MovieBasicResponseDto toBasicDTO(@NotNull Movie entity);

    MovieBasicInfoResponseDto toBasicInfoDTO(@NotNull Movie entity);
}
