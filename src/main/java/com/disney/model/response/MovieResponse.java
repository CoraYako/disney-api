package com.disney.model.response;

import com.disney.model.response.basic.CharacterBasicResponse;
import com.disney.model.response.basic.GenreBasicResponse;

import java.util.List;


public class MovieResponse {

    private Long id;

    private String image;

    private String title;

    private String creation;

    private Integer rate;

    private GenreBasicResponse genre;

    private List<CharacterBasicResponse> characters;
}
