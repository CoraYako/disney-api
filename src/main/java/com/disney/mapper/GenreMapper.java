package com.disney.mapper;

import com.disney.dto.GenreDTO;
import com.disney.entity.GenreEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenreMapper {

    /*@Autowired
    private MovieMapper movieMapper;*/
    //TODO add movie list conversion
    public GenreDTO entity2DTO(GenreEntity entity) {
        GenreDTO dto = new GenreDTO();

        dto.setId(entity.getId());
        dto.setImage(entity.getImage());
        dto.setName(entity.getName());

        return dto;
    }

    public GenreEntity DTO2Entyti(GenreDTO dto) {
        GenreEntity entity = new GenreEntity();

        entity.setImage(dto.getImage());
        entity.setName(dto.getName());

        return entity;
    }

}
