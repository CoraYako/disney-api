package com.disney.service;

import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.request.GenreUpdateRequestDto;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.model.entity.Genre;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface GenreService {

    void createGenre(@NotNull GenreRequestDto requestDto);

    GenreResponseDto updateGenre(@NotNull UUID id, GenreUpdateRequestDto requestDto);

    GenreResponseDto getGenreDtoById(@NotNull UUID id);

    Genre getGenreById(@NotNull UUID id);
}
