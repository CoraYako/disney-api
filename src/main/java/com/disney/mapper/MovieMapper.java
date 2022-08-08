package com.disney.mapper;

import com.disney.dto.MovieDTO;
import com.disney.entity.MovieEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MovieMapper {

    /*@Autowired
    private CharacterMapper characterMapper;
    @Autowired
    private GenreMapper genreMapper;*/

    //TODO add character list conversion
    private MovieDTO entity2DTO(MovieEntity entity) {
        MovieDTO dto = new MovieDTO();

        dto.setId(entity.getId());
        dto.setImage(entity.getImage());
        dto.setTitle(entity.getTitle());
        dto.setCreation(entity.getCreation());
        dto.setRate(entity.getRate());
        /*dto.setGenre(genreMapper.entity2DTO(entity.getGenre()));*/

        return dto;
    }

    private MovieEntity DTO2Entity(MovieDTO dto) {
        MovieEntity entity = new MovieEntity();

        entity.setImage(dto.getImage());
        entity.setTitle(dto.getTitle());
        entity.setCreation(dto.getCreation());
        entity.setRate(dto.getRate());
        /*entity.setGenre(genreMapper.DTO2Entyti(dto.getGenre()));*/

        return entity;
    }

    private List<MovieDTO> entityList2DTOList(List<MovieEntity> entityList) {
        List<MovieDTO> dtoList = new ArrayList<>();
        entityList.forEach(entity -> dtoList.add(entity2DTO(entity)));
        return dtoList;
    }

    private List<MovieEntity> DTOList2EntityList(List<MovieDTO> dtoList) {
        List<MovieEntity> entityList = new ArrayList<>();
        dtoList.forEach(dto -> entityList.add(DTO2Entity(dto)));
        return entityList;
    }

}
