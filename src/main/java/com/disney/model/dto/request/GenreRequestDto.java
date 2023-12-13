package com.disney.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record GenreRequestDto(
        @NotBlank(message = "The name can't be whitespaces")
        @NotEmpty(message = "The name can't be empty or null")
        String name
) {
    public static GenreRequestDtoBuilder builder() {
        return new GenreRequestDtoBuilder();
    }

    public static class GenreRequestDtoBuilder {
        private String name;

        public GenreRequestDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GenreRequestDto build() {
            return new GenreRequestDto(name);
        }
    }
}
