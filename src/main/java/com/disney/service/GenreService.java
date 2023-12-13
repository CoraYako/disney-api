package com.disney.service;

import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.request.GenreUpdateRequestDto;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.model.entity.Genre;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface GenreService {

    void createGenre(@NotNull GenreRequestDto requestDto);

    GenreResponseDto updateGenre(@NotNull String id, GenreUpdateRequestDto requestDto);

    GenreResponseDto getGenreById(@NotNull String id);

    Genre getGenreById(@NotNull UUID id);

    Page<GenreResponseDto> listMovieGenres(int pageNumber);
}
