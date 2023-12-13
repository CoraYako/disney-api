package com.disney.model.mapper.implementation;

import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.model.dto.response.basic.GenreBasicResponseDto;
import com.disney.model.entity.Genre;
import com.disney.model.mapper.GenreMapper;
import com.disney.model.mapper.MovieMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import static java.util.stream.Collectors.toUnmodifiableSet;

@Component
@Validated
public class GenreMapperImpl implements GenreMapper {
    private final MovieMapper movieMapper;

    public GenreMapperImpl(@Lazy MovieMapper movieMapper) {
        this.movieMapper = movieMapper;
    }

    @Override
    public Genre toEntity(GenreRequestDto dto) {
        return Genre.builder()
                .name(dto.name())
                .build();
    }

    @Override
    public GenreResponseDto toDTO(Genre entity) {
        return GenreResponseDto.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .movies(entity.getMovies().stream().map(movieMapper::toBasicDTO).collect(toUnmodifiableSet()))
                .build();
    }

    @Override
    public GenreBasicResponseDto toBasicDTO(Genre entity) {
        return GenreBasicResponseDto.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .build();
    }
}
