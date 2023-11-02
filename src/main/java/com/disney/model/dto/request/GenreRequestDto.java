package com.disney.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record GenreRequestDto(
        @NotBlank(message = "The name can't be whitespaces")
        @NotEmpty(message = "The name can't be empty or null")
        String name,
        @NotBlank(message = "The image can't be whitespaces")
        @NotEmpty(message = "The image can't be empty or null")
        String image
) {
}
