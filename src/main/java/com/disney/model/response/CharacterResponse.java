package com.disney.model.response;

import com.disney.model.response.basic.MovieBasicResponse;

import java.util.List;


public class CharacterResponse {

    private Long id;

    private String image;

    private String name;

    private Integer age;

    private Double weight;

    private String history;

    private List<MovieBasicResponse> movies;
}
