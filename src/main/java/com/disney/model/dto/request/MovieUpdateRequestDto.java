package com.disney.model.dto.request;

import java.util.Set;

public record MovieUpdateRequestDto(
        String image,
        String title,
        String creationDate,
        int rate,
        String genreId,
        Set<String> charactersToAdd,
        Set<String> charactersToRemove
) {
    public static MovieUpdateRequestDtoBuilder builder() {
        return new MovieUpdateRequestDtoBuilder();
    }

    public static class MovieUpdateRequestDtoBuilder {
        private String image;
        private String title;
        private String creationDate;
        private int rate;
        private String genreId;
        private Set<String> charactersToAdd;
        private Set<String> charactersToRemove;

        public MovieUpdateRequestDtoBuilder image(String image) {
            this.image = image;
            return this;
        }

        public MovieUpdateRequestDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MovieUpdateRequestDtoBuilder creationDate(String creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public MovieUpdateRequestDtoBuilder rate(int rate) {
            this.rate = rate;
            return this;
        }

        public MovieUpdateRequestDtoBuilder genreId(String genreId) {
            this.genreId = genreId;
            return this;
        }

        public MovieUpdateRequestDtoBuilder charactersToAdd(Set<String> charactersToAdd) {
            this.charactersToAdd = charactersToAdd;
            return this;
        }

        public MovieUpdateRequestDtoBuilder charactersToRemove(Set<String> charactersToRemove) {
            this.charactersToRemove = charactersToRemove;
            return this;
        }

        public MovieUpdateRequestDto build() {
            return new MovieUpdateRequestDto(image, title, creationDate, rate, genreId, charactersToAdd, charactersToRemove);
        }
    }
}
