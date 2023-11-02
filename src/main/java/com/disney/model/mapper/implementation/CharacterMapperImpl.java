package com.disney.model.mapper.implementation;

import com.disney.model.dto.request.CharacterRequestDto;
import com.disney.model.dto.response.CharacterResponseDto;
import com.disney.model.dto.response.basic.CharacterBasicResponseDto;
import com.disney.model.entity.Character;
import com.disney.model.mapper.CharacterMapper;
import com.disney.model.mapper.MovieMapper;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.stream.Collectors;

@Component
@Validated
public class CharacterMapperImpl implements CharacterMapper {
    private final MovieMapper movieMapper;

    public CharacterMapperImpl(MovieMapper movieMapper) {
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
        return new CharacterResponseDto(
                entity.getId().toString(),
                entity.getImage(),
                entity.getName(),
                entity.getAge(),
                entity.getWeight(),
                entity.getHistory(),
                entity.getMovies().stream()
                        .map(movieMapper::toBasicInfoDTO)
                        .collect(Collectors.toUnmodifiableSet())
        );
    }

    @Override
    public CharacterBasicResponseDto toBasicDTO(Character entity) {
        return new CharacterBasicResponseDto(
                entity.getId().toString(),
                entity.getImage(),
                entity.getName(),
                entity.getAge(),
                entity.getWeight(),
                entity.getHistory()
        );
    }
}
