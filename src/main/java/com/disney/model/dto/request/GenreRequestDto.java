package com.disney.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record GenreRequestDto(
        @NotBlank(message = "The name can't be whitespaces")
        @NotEmpty(message = "The name can't be empty or null")
        String name
) {
}
