package com.disney.model.mapper.implementation;

import com.disney.model.dto.request.MovieRequestDto;
import com.disney.model.dto.response.MovieResponseDto;
import com.disney.model.dto.response.basic.MovieBasicInfoResponseDto;
import com.disney.model.dto.response.basic.MovieBasicResponseDto;
import com.disney.model.entity.Movie;
import com.disney.model.mapper.CharacterMapper;
import com.disney.model.mapper.GenreMapper;
import com.disney.model.mapper.MovieMapper;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

import static com.disney.util.ApiUtils.OF_PATTERN;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Component
@Validated
public class MovieMapperImpl implements MovieMapper {
    private final GenreMapper genreMapper;
    private final CharacterMapper characterMapper;

    public MovieMapperImpl(GenreMapper genreMapper, CharacterMapper characterMapper) {
        this.genreMapper = genreMapper;
        this.characterMapper = characterMapper;
    }

    @Override
    public Movie toEntity(MovieRequestDto dto) {
        return Movie.builder()
                .image(dto.image())
                .title(dto.title())
                .creationDate(LocalDate.parse(dto.creationDate(), OF_PATTERN))
                .rate(dto.rate())
                .build();
    }

    @Override
    public MovieResponseDto toDTO(Movie entity) {
        return MovieResponseDto.builder()
                .id(entity.getId().toString())
                .image(entity.getImage())
                .title(entity.getTitle())
                .creationDate(entity.getCreationDate().format(OF_PATTERN))
                .rate(entity.getRate())
                .genre(genreMapper.toBasicDTO(entity.getGenre()))
                .characters(entity.getCharacters().stream()
                        .map(characterMapper::toBasicDTO).collect(toUnmodifiableSet()))
                .build();
    }

    @Override
    public MovieBasicResponseDto toBasicDTO(Movie entity) {
        return MovieBasicResponseDto.builder()
                .id(entity.getId().toString())
                .image(entity.getImage())
                .title(entity.getTitle())
                .creationDate(entity.getCreationDate().format(OF_PATTERN))
                .rate(entity.getRate())
                .character(entity.getCharacters().stream()
                        .map(characterMapper::toBasicDTO).collect(toUnmodifiableSet()))
                .build();
    }

    @Override
    public MovieBasicInfoResponseDto toBasicInfoDTO(Movie entity) {
        return MovieBasicInfoResponseDto.builder()
                .id(entity.getId().toString())
                .image(entity.getImage())
                .title(entity.getTitle())
                .creationDate(entity.getCreationDate().format(OF_PATTERN))
                .rate(entity.getRate())
                .genre(genreMapper.toBasicDTO(entity.getGenre()))
                .build();
    }
}
