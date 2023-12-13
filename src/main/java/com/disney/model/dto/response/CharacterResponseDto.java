package com.disney.model.dto.response;

import com.disney.model.dto.response.basic.MovieBasicInfoResponseDto;

import java.util.Set;

public record CharacterResponseDto(
        String id,
        String image,
        String name,
        int age,
        double weight,
        String history,
        Set<MovieBasicInfoResponseDto> movies
) {
    public static CharacterResponseDtoBuilder builder() {
        return new CharacterResponseDtoBuilder();
    }

    public static class CharacterResponseDtoBuilder {
        private String id;
        private String image;
        private String name;
        private int age;
        private double weight;
        private String history;
        private Set<MovieBasicInfoResponseDto> movies;

        public CharacterResponseDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public CharacterResponseDtoBuilder image(String image) {
            this.image = image;
            return this;
        }

        public CharacterResponseDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CharacterResponseDtoBuilder age(int age) {
            this.age = age;
            return this;
        }

        public CharacterResponseDtoBuilder weight(double weight) {
            this.weight = weight;
            return this;
        }

        public CharacterResponseDtoBuilder history(String history) {
            this.history = history;
            return this;
        }

        public CharacterResponseDtoBuilder movies(Set<MovieBasicInfoResponseDto> movies) {
            this.movies = movies;
            return this;
        }

        public CharacterResponseDto build() {
            return new CharacterResponseDto(id, image, name, age, weight, history, movies);
        }
    }
}
