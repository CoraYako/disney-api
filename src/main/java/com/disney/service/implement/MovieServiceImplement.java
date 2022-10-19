package com.disney.service.implement;

import com.disney.model.entity.MovieEntity;
import com.disney.model.mapper.MovieMapper;
import com.disney.model.request.MovieRequest;
import com.disney.model.response.MovieResponse;
import com.disney.model.response.basic.MovieBasicResponseList;
import com.disney.repository.MovieRepository;
import com.disney.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImplement implements MovieService {

    private final MovieRepository repository;

    private final MovieMapper mapper;

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
    @Transactional(readOnly = true)
    public MovieBasicResponseList getAll() {
        return mapper.entityList2DTOList(repository.findAll());
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
}
