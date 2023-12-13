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
        Set<String> moviesId
) {
    public static CharacterRequestDtoBuilder builder() {
        return new CharacterRequestDtoBuilder();
    }

    public static class CharacterRequestDtoBuilder {
        private String image;
        private String name;
        private int age;
        private double weight;
        private String history;
        private Set<String> moviesId;

        public CharacterRequestDtoBuilder image(String image) {
            this.image = image;
            return this;
        }

        public CharacterRequestDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CharacterRequestDtoBuilder age(int age) {
            this.age = age;
            return this;
        }

        public CharacterRequestDtoBuilder weight(double weight) {
            this.weight = weight;
            return this;
        }

        public CharacterRequestDtoBuilder history(String history) {
            this.history = history;
            return this;
        }

        public CharacterRequestDtoBuilder moviesId(Set<String> moviesId) {
            this.moviesId = moviesId;
            return this;
        }

        public CharacterRequestDto build() {
            return new CharacterRequestDto(image, name, age, weight, history, moviesId);
        }
    }
}
