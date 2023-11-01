package com.disney.model.request;

import com.disney.model.response.CharacterResponse;
import com.disney.model.response.GenreResponse;

import jakarta.validation.constraints.*;
import java.util.List;


public class MovieRequest {

    @NotEmpty(message = "The image cant be empty or null")
    @NotBlank(message = "The image can't be whitespaces")
    private String image;

    @NotEmpty(message = "The title cant be empty or null")
    @NotBlank(message = "The title can't be whitespaces")
    private String title;

    @NotEmpty(message = "The creation date cant be empty or null")
    @NotBlank(message = "The creation date can't be whitespaces")
    private String creation;

    @NotNull
    @Min(value = 1, message = "Positive values only, the minimum is 1")
    @Max(value = 5, message = "Positive values only, the maximum is 5")
    private Integer rate;

    @NotNull(message = "List of characters are required")
    @NotEmpty(message = "Character list cannot be empty")
    private List<CharacterResponse> charactersId;

    @NotNull(message = "The genre can't be null")
    private GenreResponse genreId;
}
