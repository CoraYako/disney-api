package com.disney.mapper;

import com.disney.dto.CharacterDTO;
import com.disney.entity.CharacterEntity;
import org.springframework.stereotype.Component;

@Component
public class CharacterMapper {
//TODO complete all the dto and entity conversion. Add movie list and the necessary beans
    public CharacterDTO entity2DTO(CharacterEntity entity) {
        CharacterDTO dto = new CharacterDTO();

        dto.setId(entity.getId());
        dto.setImage(entity.getImage());
        dto.setAge(entity.getAge());
        dto.setWeight(entity.getWeight());
        dto.setHistory(entity.getHistory());

        return dto;
    }

    public CharacterEntity DTO2Entity(CharacterDTO dto) {
        CharacterEntity entity = new CharacterEntity();

        entity.setImage(dto.getImage());
        entity.setAge(dto.getAge());
        entity.setWeight(dto.getWeight());
        entity.setHistory(dto.getHistory());

        return entity;
    }

}
