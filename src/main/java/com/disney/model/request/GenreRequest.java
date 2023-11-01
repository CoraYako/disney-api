package com.disney.model.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class GenreRequest {

    @NotBlank(message = "The name can't be whitespaces")
    @NotEmpty(message = "The name can't be empty or null")
    private String name;

    @NotBlank(message = "The image can't be whitespaces")
    @NotEmpty(message = "The image can't be empty or null")
    private String image;
}
