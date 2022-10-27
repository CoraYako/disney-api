package com.disney.service.implement;

import com.disney.model.entity.CharacterEntity;
import com.disney.model.mapper.CharacterMapper;
import com.disney.model.request.CharacterFilterRequest;
import com.disney.model.request.CharacterRequest;
import com.disney.model.response.CharacterResponse;
import com.disney.model.response.basic.CharacterBasicResponseList;
import com.disney.repository.CharacterRepository;
import com.disney.repository.specification.CharacterSpecification;
import com.disney.service.CharacterService;
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
public class CharacterServiceImplement implements CharacterService {

    private final CharacterMapper mapper;

    private final CharacterRepository repository;

    private final CharacterSpecification specification;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CharacterResponse save(CharacterRequest request) {
        CharacterEntity entity = getEntityByName(request.getName());
        if (entity != null) {
            throw new EntityExistsException(String.format("The character %s already exist in the database", request.getName()));
        }
        entity = mapper.DTO2Entity(request);
        return mapper.entity2DTO(repository.save(entity));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CharacterResponse update(Long id, CharacterRequest request) {
        CharacterEntity entityFound = getEntityByName(request.getName());
        if (entityFound != null) {
            throw new EntityExistsException(String.format("The character %s already exist in the database", request.getName()));
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
    public CharacterEntity getEntityById(Long id) {
        Optional<CharacterEntity> response = repository.findById(id);
        return response.orElseThrow(() -> new EntityNotFoundException("Character not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CharacterEntity> getCharactersById(List<CharacterResponse> charactersId) {
        return charactersId.stream().map(character -> getEntityById(character.getId())).collect(Collectors.toList());
    }

    @Override
    public CharacterResponse getResponseById(Long id) {
        return mapper.entity2DTO(getEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterEntity getEntityByName(String name) {
        Optional<CharacterEntity> response = repository.findByName(name);
        return response.orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterBasicResponseList getByFilters(String name, Integer age, List<Long> movies) {
        List<CharacterEntity> entityList = repository.findAll(
                specification.getByFilters(new CharacterFilterRequest(name, age, movies)));
        if (entityList.isEmpty()) {
            throw new NoSuchElementException("Character was not found for parameters {name=" + name + ", age=" + age + ", movies=" + movies + '}');
        }
        return mapper.entityList2DTOList(entityList);
    }
}
