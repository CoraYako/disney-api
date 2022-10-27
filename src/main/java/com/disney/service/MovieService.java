package com.disney.service;

import com.disney.model.entity.MovieEntity;
import com.disney.model.request.MovieRequest;
import com.disney.model.response.MovieResponse;
import com.disney.model.response.basic.MovieBasicResponseList;

import java.util.List;

public interface MovieService {

    MovieResponse save(MovieRequest request);

    MovieResponse update(Long id, MovieRequest request);

    void delete(Long id);

    MovieEntity getEntityById(Long id);

    MovieResponse getResponseById(Long id);

    MovieEntity getEntityByTitle(String title);

    List<MovieEntity> getMoviesById(List<MovieResponse> moviesId);

    MovieBasicResponseList getByFilters(String title, Long genre, String order);
}
