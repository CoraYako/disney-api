package com.disney.model.dto.response;

import com.disney.model.dto.response.basic.CharacterBasicResponseDto;
import com.disney.model.dto.response.basic.GenreBasicResponseDto;

import java.util.Set;

public record MovieResponseDto(
        String id,
        String image,
        String title,
        String creationDate,
        int rate,
        GenreBasicResponseDto genre,
        Set<CharacterBasicResponseDto> characters
) {
    public static MovieResponseDtoBuilder builder() {
        return new MovieResponseDtoBuilder();
    }

    public static class MovieResponseDtoBuilder {
        private String id;
        private String image;
        private String title;
        private String creationDate;
        private int rate;
        private GenreBasicResponseDto genre;
        private Set<CharacterBasicResponseDto> characters;

        public MovieResponseDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MovieResponseDtoBuilder image(String image) {
            this.image = image;
            return this;
        }

        public MovieResponseDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MovieResponseDtoBuilder creationDate(String creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public MovieResponseDtoBuilder rate(int rate) {
            this.rate = rate;
            return this;
        }

        public MovieResponseDtoBuilder genre(GenreBasicResponseDto genre) {
            this.genre = genre;
            return this;
        }

        public MovieResponseDtoBuilder characters(Set<CharacterBasicResponseDto> characters) {
            this.characters = characters;
            return this;
        }

        public MovieResponseDto build() {
            return new MovieResponseDto(id, image, title, creationDate, rate, genre, characters);
        }
    }
}
