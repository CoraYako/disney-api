package com.disney.model.dto.response.basic;

public record MovieBasicInfoResponseDto(
        String id,
        String image,
        String title,
        String creationDate,
        int rate,
        GenreBasicResponseDto genre
) {
    public static MovieBasicInfoResponseDtoBuilder builder() {
        return new MovieBasicInfoResponseDtoBuilder();
    }

    public static class MovieBasicInfoResponseDtoBuilder {
        private String id;
        private String image;
        private String title;
        private String creationDate;
        private int rate;
        private GenreBasicResponseDto genre;

        public MovieBasicInfoResponseDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MovieBasicInfoResponseDtoBuilder image(String image) {
            this.image = image;
            return this;
        }

        public MovieBasicInfoResponseDtoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public MovieBasicInfoResponseDtoBuilder creationDate(String creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public MovieBasicInfoResponseDtoBuilder rate(int rate) {
            this.rate = rate;
            return this;
        }

        public MovieBasicInfoResponseDtoBuilder genre(GenreBasicResponseDto genre) {
            this.genre = genre;
            return this;
        }

        public MovieBasicInfoResponseDto build() {
            return new MovieBasicInfoResponseDto(id, image, title, creationDate, rate, genre);
        }
    }
}
