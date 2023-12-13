package com.disney.model.dto.response.basic;

public record GenreBasicResponseDto(
        String id,
        String name
) {
    public static GenreBasicResponseDtoBuilder builder() {
        return new GenreBasicResponseDtoBuilder();
    }

    public static class GenreBasicResponseDtoBuilder {
        private String id;
        private String name;

        public GenreBasicResponseDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public GenreBasicResponseDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public GenreBasicResponseDto build() {
            return new GenreBasicResponseDto(id, name);
        }
    }
}
