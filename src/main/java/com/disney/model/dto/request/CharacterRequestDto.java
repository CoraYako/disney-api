package com.disney.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CharacterRequestDto(
        String image,
        @NotEmpty(message = "The name can't be empty or null")
        @NotBlank(message = "The name can't be whitespaces")
        String name,
        @NotNull(message = "The age can't be null")
        @Min(value = 1, message = "Positive numbers only, minimum is 1")
        int age,
        @NotNull(message = "The weight can't be null")
        @Min(value = 1, message = "Positive numbers only, minimum is 1")
        double weight,
        @NotEmpty(message = "The history can't be empty or null")
        @NotBlank(message = "The history can't be whitespaces")
        String history,
        @NotNull(message = "Must provide the movie/s")
        @NotEmpty(message = "Must provide the movie/s")
        Set<String> moviesId
) {
}
