package com.disney.service;

import com.disney.model.entity.CharacterEntity;
import com.disney.model.request.CharacterRequest;
import com.disney.model.response.CharacterResponse;
import com.disney.model.response.basic.CharacterBasicResponseList;

import java.util.List;

public interface CharacterService {

    CharacterResponse save(CharacterRequest request);

    CharacterResponse update(Long id, CharacterRequest request);

    void delete(Long id);

    CharacterBasicResponseList getAll();

    CharacterEntity getEntityById(Long id);

    List<CharacterEntity> getCharactersById(List<CharacterResponse> charactersId);

    CharacterResponse getResponseById(Long id);

    CharacterEntity getEntityByName(String name);
}
