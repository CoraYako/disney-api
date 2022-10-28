package com.disney.service.implement;

import com.disney.model.entity.CharacterEntity;
import com.disney.model.entity.MovieEntity;
import com.disney.model.mapper.MovieMapper;
import com.disney.model.request.MovieFilterRequest;
import com.disney.model.request.MovieRequest;
import com.disney.model.response.MovieResponse;
import com.disney.model.response.basic.MovieBasicResponseList;
import com.disney.repository.MovieRepository;
import com.disney.repository.specification.MovieSpecification;
import com.disney.service.CharacterService;
import com.disney.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImplement implements MovieService {

    private final MovieRepository repository;

    private final MovieMapper mapper;

    private final MovieSpecification specification;

    private final CharacterService characterService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MovieResponse save(MovieRequest request) {
        MovieEntity entity = getEntityByTitle(request.getTitle());
        if (entity != null) {
            throw new EntityExistsException(String.format("The movie %s already exist in the database", request.getTitle()));
        }
        entity = repository.save(mapper.DTO2Entity(request));
        return mapper.entity2DTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MovieResponse update(Long id, MovieRequest request) {
        MovieEntity entityFound = getEntityByTitle(request.getTitle());
        if (entityFound != null) {
            throw new EntityExistsException(String.format("The movie %s already exist in the database", request.getTitle()));
        }
        entityFound = mapper.refreshValues(request, getEntityById(id));
        return mapper.entity2DTO(repository.save(entityFound));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MovieResponse addCharacter(Long id, Long characterId) {
        MovieEntity entityFound = getEntityById(id);
        CharacterEntity character = characterService.getEntityById(characterId);
        if (entityFound.getCharacters().contains(character)) {
            throw new IllegalArgumentException(String.format("The character %s is already in the character's movie list", character.getName()));
        }
        entityFound.getCharacters().add(character);
        return mapper.entity2DTO(repository.save(entityFound));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MovieResponse removeCharacter(Long id, Long characterId) {
        MovieEntity entityFound = getEntityById(id);
        CharacterEntity character = characterService.getEntityById(characterId);
        entityFound.getCharacters().remove(character);
        return mapper.entity2DTO(repository.save(entityFound));
    }

    @Override
    @Transactional(readOnly = true)
    public MovieEntity getEntityById(Long id) {
        Optional<MovieEntity> response = repository.findById(id);
        return response.orElseThrow(() -> new EntityNotFoundException("Movie not found= null"));
    }

    @Override
    public MovieResponse getResponseById(Long id) {
        return mapper.entity2DTO(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public MovieEntity getEntityByTitle(String title) {
        Optional<MovieEntity> response = repository.findByTitle(title);
        return response.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieEntity> getMoviesById(List<MovieResponse> moviesId) {
        return moviesId.stream().map(movie -> getEntityById(movie.getId())).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MovieBasicResponseList getByFilters(String title, Long genre, String order) {
        List<MovieEntity> entityList = repository.findAll(
                specification.getByFilters(new MovieFilterRequest(title, genre, order)));
        if (entityList.isEmpty()) {
            throw new NoSuchElementException("Movie was not found for parameters {title=" + title + ", genre=" + genre + '}');
        }
        return mapper.entityList2DTOList(entityList);
    }
}
