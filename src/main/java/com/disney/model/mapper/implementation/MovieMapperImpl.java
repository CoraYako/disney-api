package com.disney.model.mapper.implementation;

import com.disney.model.dto.request.MovieRequestDto;
import com.disney.model.dto.response.MovieResponseDto;
import com.disney.model.dto.response.basic.MovieBasicInfoResponseDto;
import com.disney.model.dto.response.basic.MovieBasicResponseDto;
import com.disney.model.entity.Movie;
import com.disney.model.mapper.CharacterMapper;
import com.disney.model.mapper.GenreMapper;
import com.disney.model.mapper.MovieMapper;
import com.disney.util.ApiUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.stream.Collectors;

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
                .creationDate(LocalDate.parse(dto.creationDate(), ApiUtils.OF_PATTERN))
                .rate(dto.rate())
                .build();
    }

    @Override
    public MovieResponseDto toDTO(Movie entity) {
        return new MovieResponseDto(
                entity.getId().toString(),
                entity.getImage(),
                entity.getTitle(),
                entity.getCreationDate().format(ApiUtils.OF_PATTERN),
                entity.getRate(),
                genreMapper.toBasicDTO(entity.getGenre()),
                entity.getCharacters().stream()
                        .map(characterMapper::toBasicDTO)
                        .collect(Collectors.toUnmodifiableSet())
        );
    }

    @Override
    public MovieBasicResponseDto toBasicDTO(Movie entity) {
        return new MovieBasicResponseDto(
                entity.getId().toString(),
                entity.getImage(),
                entity.getTitle(),
                entity.getCreationDate().format(ApiUtils.OF_PATTERN),
                entity.getRate(),
                entity.getCharacters().stream()
                        .map(characterMapper::toBasicDTO)
                        .collect(Collectors.toUnmodifiableSet())
        );
    }

    @Override
    public MovieBasicInfoResponseDto toBasicInfoDTO(Movie entity) {
        return new MovieBasicInfoResponseDto(
                entity.getId().toString(),
                entity.getImage(),
                entity.getTitle(),
                entity.getCreationDate().format(ApiUtils.OF_PATTERN),
                entity.getRate(),
                genreMapper.toBasicDTO(entity.getGenre())
        );
    }
}
