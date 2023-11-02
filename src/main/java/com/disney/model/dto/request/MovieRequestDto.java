package com.disney.model.dto.request;

import com.disney.model.dto.response.GenreResponseDto;
import jakarta.validation.constraints.*;

import java.util.Set;

public record MovieRequestDto(
        String image,
        @NotEmpty(message = "The title cant be empty or null")
        @NotBlank(message = "The title can't be whitespaces")
        String title,
        @NotEmpty(message = "The creation date cant be empty or null")
        @NotBlank(message = "The creation date can't be whitespaces")
        String creationDate,
        @NotNull
        @Min(value = 1, message = "Positive values only, the minimum is 1")
        @Max(value = 5, message = "Positive values only, the maximum is 5")
        Integer rate,
        @NotNull(message = "The genre can't be null")
        GenreResponseDto genreId,
        Set<String> charactersId
) {
}
