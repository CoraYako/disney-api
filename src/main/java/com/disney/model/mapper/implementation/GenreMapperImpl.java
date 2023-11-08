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

import java.util.stream.Collectors;

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
        return new GenreResponseDto(
                entity.getId().toString(),
                entity.getName(),
                entity.getMovies().stream()
                        .map(movieMapper::toBasicDTO)
                        .collect(Collectors.toUnmodifiableSet())
        );
    }

    @Override
    public GenreBasicResponseDto toBasicDTO(Genre entity) {
        return new GenreBasicResponseDto(
                entity.getId().toString(),
                entity.getName()
        );
    }
}
