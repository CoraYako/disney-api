package com.disney.model.dto.request;

public record GenreUpdateRequestDto(
        String name
) {
    public static GenreUpdateRequestDtoBuilder builder() {
        return new GenreUpdateRequestDtoBuilder();
    }

    public static class GenreUpdateRequestDtoBuilder {
        private String name;

        public GenreUpdateRequestDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GenreUpdateRequestDto build() {
            return new GenreUpdateRequestDto(name);
        }
    }
}
