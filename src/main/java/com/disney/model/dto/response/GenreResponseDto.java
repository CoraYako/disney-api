package com.disney.model.dto.response;

import com.disney.model.dto.response.basic.MovieBasicResponseDto;

import java.util.Set;

public record GenreResponseDto(
        String id,
        String name,
        Set<MovieBasicResponseDto> movies
) {
    public static GenreResponseDtoBuilder builder() {
        return new GenreResponseDtoBuilder();
    }

    public static class GenreResponseDtoBuilder {
        private String id;
        private String name;
        private Set<MovieBasicResponseDto> movies;

        public GenreResponseDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public GenreResponseDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GenreResponseDtoBuilder movies(Set<MovieBasicResponseDto> movies) {
            this.movies = movies;
            return this;
        }

        public GenreResponseDto build() {
            return new GenreResponseDto(id, name, movies);
        }
    }
}
