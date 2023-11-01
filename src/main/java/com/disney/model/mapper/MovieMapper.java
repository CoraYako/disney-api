package com.disney.model.mapper;

import com.disney.model.entity.MovieEntity;
import com.disney.model.request.MovieRequest;
import com.disney.model.response.MovieResponse;
import com.disney.model.response.basic.MovieBasicResponse;
import com.disney.model.response.basic.MovieBasicResponseList;
import com.disney.service.CharacterService;
import com.disney.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MovieMapper {

    private final CharacterMapper characterMapper;

    private final CharacterService characterService;

    private final GenreMapper genreMapper;

    private final GenreService genreService;

    public MovieMapper(CharacterMapper characterMapper, CharacterService characterService, GenreMapper genreMapper, GenreService genreService) {
        this.characterMapper = characterMapper;
        this.characterService = characterService;
        this.genreMapper = genreMapper;
        this.genreService = genreService;
    }

    public MovieEntity DTO2Entity(MovieRequest request) {
        MovieEntity entity = new MovieEntity();
        entity.setImage(request.getImage());
        entity.setTitle(request.getTitle());
        if (request.getCreation() != null && !request.getCreation().trim().isEmpty()) {
            entity.setCreation(string2LocalDate(request.getCreation()));
        }
        entity.setRate(request.getRate());
        if (request.getGenreId() != null) {
            entity.setGenre(genreService.getEntityById(request.getGenreId().getId()));
        }
        if (request.getCharactersId() != null && !request.getCharactersId().isEmpty()) {
            entity.setCharacters(characterService.getCharactersById(request.getCharactersId()));
        }
        return entity;
    }

    public MovieResponse entity2DTO(MovieEntity entity) {
        MovieResponse response = new MovieResponse();
        response.setId(entity.getId());
        response.setImage(entity.getImage());
        response.setTitle(entity.getTitle());
        response.setCreation(localDate2String(entity.getCreation()));
        response.setRate(entity.getRate());
        response.setGenre(genreMapper.entity2BasicDTO(entity.getGenre()));
        response.setCharacters(entity.getCharacters().stream().map(characterMapper::entity2BasicDTO).collect(Collectors.toList()));
        return response;
    }

    public MovieBasicResponse entity2BasicDTO(MovieEntity entity) {
        MovieBasicResponse basicResponse = new MovieBasicResponse();
        basicResponse.setId(entity.getId());
        basicResponse.setImage(entity.getImage());
        basicResponse.setTitle(entity.getTitle());
        basicResponse.setCreation(localDate2String(entity.getCreation()));
        return basicResponse;
    }

    public MovieEntity refreshValues(MovieRequest request, MovieEntity entity2Return) {
        MovieEntity entity = DTO2Entity(request);
        if (entity.getImage() != null && !entity.getImage().trim().isEmpty()) {
            entity2Return.setImage(entity.getImage());
        }
        if (entity.getTitle() != null && !entity.getTitle().trim().isEmpty()) {
            entity2Return.setTitle(entity.getTitle());
        }
        if (entity.getCreation() != null) {
            entity2Return.setCreation(entity.getCreation());
        }
        if (entity.getRate() != null && entity.getRate() >= 1 && entity.getRate() <= 5) {
            entity2Return.setRate(entity.getRate());
        }
        if (request.getCharactersId() != null && !request.getCharactersId().isEmpty()) {
            entity2Return.setCharacters(request.getCharactersId().stream().map(character -> characterService.getEntityById(character.getId())).collect(Collectors.toList()));
        }
        if (request.getGenreId() != null) {
            entity2Return.setGenre(genreService.getEntityById(request.getGenreId().getId()));
        }
        return entity2Return;
    }

    public MovieBasicResponseList entityList2DTOList(List<MovieEntity> entityList) {
        MovieBasicResponseList basicResponseList = new MovieBasicResponseList();
        basicResponseList.setMovies(entityList.stream().map(this::entity2BasicDTO).collect(Collectors.toList()));
        return basicResponseList;
    }

    public LocalDate string2LocalDate(String stringDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return LocalDate.parse(stringDate, formatter);
    }

    public String localDate2String(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
}
