package com.disney.model.mapper.implementation;

import com.disney.model.dto.request.CharacterRequestDto;
import com.disney.model.dto.response.CharacterResponseDto;
import com.disney.model.dto.response.basic.CharacterBasicResponseDto;
import com.disney.model.entity.Character;
import com.disney.model.mapper.CharacterMapper;
import com.disney.model.mapper.MovieMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import static java.util.stream.Collectors.toUnmodifiableSet;

@Component
@Validated
public class CharacterMapperImpl implements CharacterMapper {
    private final MovieMapper movieMapper;

    public CharacterMapperImpl(@Lazy MovieMapper movieMapper) {
        this.movieMapper = movieMapper;
    }

    @Override
    public Character toEntity(CharacterRequestDto dto) {
        return Character.builder()
                .image(dto.image())
                .name(dto.name())
                .age(dto.age())
                .weight(dto.weight())
                .history(dto.history())
                .build();
    }

    @Override
    public CharacterResponseDto toDTO(Character entity) {
        return CharacterResponseDto.builder()
                .id(entity.getId().toString())
                .image(entity.getImage())
                .name(entity.getName())
                .age(entity.getAge())
                .weight(entity.getWeight())
                .history(entity.getHistory())
                .movies(entity.getMovies().stream().map(movieMapper::toBasicInfoDTO).collect(toUnmodifiableSet()))
                .build();
    }

    @Override
    public CharacterBasicResponseDto toBasicDTO(Character entity) {
        return CharacterBasicResponseDto.builder()
                .id(entity.getId().toString())
                .image(entity.getImage())
                .name(entity.getName())
                .age(entity.getAge())
                .weight(entity.getWeight())
                .history(entity.getHistory())
                .build();
    }
}
