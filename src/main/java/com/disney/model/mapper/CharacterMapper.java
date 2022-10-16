package com.disney.model.mapper;

import com.disney.model.entity.CharacterEntity;
import com.disney.model.request.CharacterRequest;
import com.disney.model.response.CharacterResponse;
import com.disney.model.response.basic.CharacterBasicResponse;
import com.disney.model.response.basic.CharacterBasicResponseList;
import com.disney.service.MovieService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CharacterMapper {

    private final MovieMapper movieMapper;

    private final MovieService movieService;

    public CharacterMapper(@Lazy MovieMapper movieMapper, @Lazy MovieService movieService) {
        this.movieMapper = movieMapper;
        this.movieService = movieService;
    }

    public CharacterEntity DTO2Entity(CharacterRequest request) {
        CharacterEntity entity = new CharacterEntity();
        entity.setImage(request.getImage());
        entity.setName(request.getName());
        entity.setAge(request.getAge());
        entity.setWeight(request.getWeight());
        entity.setHistory(request.getHistory());
        if (request.getMoviesId() != null && !request.getMoviesId().isEmpty()) {
            entity.setMovies(movieService.getMoviesById(request.getMoviesId()));
        }
        return entity;
    }

    public CharacterResponse entity2DTO(CharacterEntity entity) {
        CharacterResponse response = new CharacterResponse();
        response.setId(entity.getId());
        response.setImage(entity.getImage());
        response.setName(entity.getName());
        response.setAge(entity.getAge());
        response.setWeight(entity.getWeight());
        response.setHistory(entity.getHistory());
        if (entity.getMovies() != null && !entity.getMovies().isEmpty()) {
            response.setMovies(entity.getMovies().stream().map(movieMapper::entity2BasicDTO).collect(Collectors.toList()));
        }
        return response;
    }

    public CharacterBasicResponse entity2BasicDTO(CharacterEntity entity) {
        CharacterBasicResponse basicResponse = new CharacterBasicResponse();
        basicResponse.setId(entity.getId());
        basicResponse.setImage(entity.getImage());
        basicResponse.setName(entity.getName());
        return basicResponse;
    }

    public CharacterBasicResponseList entityList2DTOList(List<CharacterEntity> entityList) {
        CharacterBasicResponseList basicResponseList = new CharacterBasicResponseList();
        basicResponseList.setCharacters(entityList.stream().map(this::entity2BasicDTO).collect(Collectors.toList()));
        return basicResponseList;
    }

    public CharacterEntity refreshValues(CharacterRequest request, CharacterEntity entity2Return) {
        CharacterEntity entity = DTO2Entity(request);
        if (entity.getImage() != null && !entity.getImage().trim().isEmpty()) {
            entity2Return.setImage(entity.getImage());
        }
        if (entity.getName() != null && !entity.getName().trim().isEmpty()) {
            entity2Return.setName(entity.getName());
        }
        if (entity.getAge() != null && entity.getAge() > 0) {
            entity2Return.setAge(entity.getAge());
        }
        if (entity.getWeight() != null && entity.getWeight() > 0) {
            entity2Return.setWeight(entity.getWeight());
        }
        if (request.getMoviesId() != null && !request.getMoviesId().isEmpty()) {
            entity2Return.setMovies(request.getMoviesId().stream().map(movie -> movieService.getEntityById(movie.getId())).collect(Collectors.toList()));
        }
        return entity2Return;
    }
}
