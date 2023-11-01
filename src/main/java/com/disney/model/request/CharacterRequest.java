package com.disney.model.request;

import com.disney.model.response.MovieResponse;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;


public class CharacterRequest {

    @NotEmpty(message = "The image can't be empty or null")
    @NotBlank(message = "The image can't be whitespaces")
    private String image;

    @NotEmpty(message = "The name can't be empty or null")
    @NotBlank(message = "The name can't be whitespaces")
    private String name;

    @NotNull(message = "The age can't be null")
    @Min(value = 1, message = "Positive numbers only, minimum is 1")
    private Integer age;

    @NotNull(message = "The weight can't be null")
    @Min(value = 1, message = "Positive numbers only, minimum is 1")
    private Double weight;

    @NotEmpty(message = "The history can't be empty or null")
    @NotBlank(message = "The history can't be whitespaces")
    private String history;

    private List<MovieResponse> moviesId;
}
