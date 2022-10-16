package com.disney.service;

import com.disney.model.entity.GenreEntity;
import com.disney.model.request.GenreRequest;
import com.disney.model.response.GenreResponse;

public interface GenreService {

    GenreResponse save(GenreRequest request);

    GenreResponse update(Long id, GenreRequest request);

    GenreEntity getEntityById(Long id);

    GenreResponse getResponseById(Long id);

    GenreEntity getByName(String name);
}
