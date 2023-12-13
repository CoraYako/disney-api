package com.disney.model.dto.request;

import jakarta.validation.constraints.*;

import java.util.Set;

public record MovieRequestDto(
        String image,
        @NotEmpty(message = "The title cant be empty or null")
        @NotBlank(message = "The title can't be whitespaces")
        String title,
        @NotEmpty(message = "The creation date cant be empty or null")
        @NotBlank(message = "The creation date can't be whitespaces")
        String creationDate,
        @NotNull
        @Min(value = 1, message = "Positive values only, the minimum is 1")
        @Max(value = 5, message = "Positive values only, the maximum is 5")
        int rate,
        @NotNull(message = "The genre can't be null")
        String genreId,
        @NotNull
        @NotEmpty
        Set<String> charactersId
) {
    public static MovieRequestDtoBuilder builder() {
        return new MovieRequestDtoBuilder();
    }

    public static class MovieRequestDtoBuilder {
        private String image;
        private String title;
        private String creationDate;
        private int rate;
        private String genreId;
        private Set<String> charactersId;

        public MovieRequestDtoBuilder image(String image) {
            this.image = image;
            return this;
        }

        public MovieRequestDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MovieRequestDtoBuilder creationDate(String creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public MovieRequestDtoBuilder rate(int rate) {
            this.rate = rate;
            return this;
        }

        public MovieRequestDtoBuilder genreId(String genreId) {
            this.genreId = genreId;
            return this;
        }

        public MovieRequestDtoBuilder charactersId(Set<String> charactersId) {
            this.charactersId = charactersId;
            return this;
        }

        public MovieRequestDto build() {
            return new MovieRequestDto(image, title, creationDate, rate, genreId, charactersId);
        }
    }
}
