package com.disney.model.dto.request;

import java.util.Set;

public record CharacterUpdateRequestDto(
        String image,
        String name,
        int age,
        double weight,
        String history,
        Set<String> moviesWhereAppears,
        Set<String> moviesToUnlink
) {
    public static CharacterUpdateRequestDtoBuilder builder() {
        return new CharacterUpdateRequestDtoBuilder();
    }

    public static class CharacterUpdateRequestDtoBuilder {
        private String image;
        private String name;
        private int age;
        private double weight;
        private String history;
        private Set<String> moviesWhereAppears;
        private Set<String> moviesToUnlink;

        public CharacterUpdateRequestDtoBuilder image(String image) {
            this.image = image;
            return this;
        }

        public CharacterUpdateRequestDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CharacterUpdateRequestDtoBuilder age(int age) {
            this.age = age;
            return this;
        }

        public CharacterUpdateRequestDtoBuilder weight(double weight) {
            this.weight = weight;
            return this;
        }

        public CharacterUpdateRequestDtoBuilder history(String history) {
            this.history = history;
            return this;
        }

        public CharacterUpdateRequestDtoBuilder moviesWhereAppears(Set<String> moviesWhereAppears) {
            this.moviesWhereAppears = moviesWhereAppears;
            return this;
        }

        public CharacterUpdateRequestDtoBuilder moviesToUnlink(Set<String> moviesToUnlink) {
            this.moviesToUnlink = moviesToUnlink;
            return this;
        }

        public CharacterUpdateRequestDto build() {
            return new CharacterUpdateRequestDto(image, name, age, weight, history, moviesWhereAppears, moviesToUnlink);
        }
    }
}
