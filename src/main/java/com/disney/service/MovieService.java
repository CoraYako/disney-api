package com.disney.service;

import com.disney.model.dto.request.MovieRequestDto;
import com.disney.model.dto.request.MovieUpdateRequestDto;
import com.disney.model.dto.response.MovieResponseDto;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface MovieService {

    void createMovie(@NotNull MovieRequestDto requestDto);

    MovieResponseDto updateMovie(@NotNull UUID id, @NotNull MovieUpdateRequestDto request);

    void deleteMovie(@NotNull UUID id);

    MovieResponseDto getMovieDtoById(@NotNull UUID id);

    Page<MovieResponseDto> listMovies(@NotNull int pageNumber, String title, String genre, String order);
}
