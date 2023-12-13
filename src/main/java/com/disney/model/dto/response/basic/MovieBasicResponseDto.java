package com.disney.model.dto.response.basic;

import java.util.Set;

public record MovieBasicResponseDto(
        String id,
        String image,
        String title,
        String creationDate,
        int rate,
        Set<CharacterBasicResponseDto> characters
) {
    public static MovieBasicResponseDtoBuilder builder() {
        return new MovieBasicResponseDtoBuilder();
    }

    public static class MovieBasicResponseDtoBuilder {
        private String id;
        private String image;
        private String title;
        private String creationDate;
        private int rate;
        private Set<CharacterBasicResponseDto> character;

        public MovieBasicResponseDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MovieBasicResponseDtoBuilder image(String image) {
            this.image = image;
            return this;
        }

        public MovieBasicResponseDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MovieBasicResponseDtoBuilder creationDate(String creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public MovieBasicResponseDtoBuilder rate(int rate) {
            this.rate = rate;
            return this;
        }

        public MovieBasicResponseDtoBuilder character(Set<CharacterBasicResponseDto> character) {
            this.character = character;
            return this;
        }

        public MovieBasicResponseDto build() {
            return new MovieBasicResponseDto(id, image, title, creationDate, rate, character);
        }
    }
}
