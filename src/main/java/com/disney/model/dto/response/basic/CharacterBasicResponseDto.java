package com.disney.model.dto.response.basic;

public record CharacterBasicResponseDto(
        String id,
        String image,
        String name,
        int age,
        double weight,
        String history
) {
    public static CharacterBasicResponseDtoBuilder builder() {
        return new CharacterBasicResponseDtoBuilder();
    }

    public static class CharacterBasicResponseDtoBuilder {
        private String id;
        private String image;
        private String name;
        private int age;
        private double weight;
        private String history;

        public CharacterBasicResponseDtoBuilder id(String id) {
            this.id = id;
            return this;
        }

        public CharacterBasicResponseDtoBuilder image(String image) {
            this.image = image;
            return this;
        }

        public CharacterBasicResponseDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CharacterBasicResponseDtoBuilder age(int age) {
            this.age = age;
            return this;
        }

        public CharacterBasicResponseDtoBuilder weight(double weight) {
            this.weight = weight;
            return this;
        }

        public CharacterBasicResponseDtoBuilder history(String history) {
            this.history = history;
            return this;
        }

        public CharacterBasicResponseDto build() {
            return new CharacterBasicResponseDto(id, image, name, age, weight, history);
        }
    }
}
