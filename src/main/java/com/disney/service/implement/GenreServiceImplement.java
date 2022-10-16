package com.disney.service.implement;

import com.disney.model.entity.GenreEntity;
import com.disney.model.mapper.GenreMapper;
import com.disney.model.request.GenreRequest;
import com.disney.model.response.GenreResponse;
import com.disney.repository.GenreRepository;
import com.disney.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenreServiceImplement implements GenreService {

    private final GenreRepository repository;

    private final GenreMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GenreResponse save(GenreRequest request) {
        GenreEntity entityFound = getByName(request.getName());
        if (entityFound != null) {
            throw new EntityExistsException(String.format("The genre %s already exist in the database.", request.getName()));
        }
        GenreEntity entity = mapper.DTO2Entity(request);
        return mapper.entity2DTO(repository.save(entity));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GenreResponse update(Long id, GenreRequest request) {
        GenreEntity entityFound = getByName(request.getName());
        if (entityFound != null) {
            throw new EntityExistsException(String.format("The genre %s already exist in the database.", request.getName()));
        }
        entityFound = mapper.refreshValues(request, getEntityById(id));
        return mapper.entity2DTO(repository.save(entityFound));
    }

    @Override
    @Transactional(readOnly = true)
    public GenreEntity getEntityById(Long id) {
        Optional<GenreEntity> response = repository.findById(id);
        return response.orElseThrow(() -> new EntityNotFoundException("Genre not found= null"));
    }

    @Override
    public GenreResponse getResponseById(Long id) {
        GenreEntity entityFound = getEntityById(id);
        return mapper.entity2DTO(entityFound);
    }

    @Override
    @Transactional(readOnly = true)
    public GenreEntity getByName(String name) {
        Optional<GenreEntity> response = repository.findByName(name);
        return response.orElse(null);
    }
}
