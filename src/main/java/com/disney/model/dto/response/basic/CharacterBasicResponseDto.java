package com.disney.model.dto.response.basic;

public record CharacterBasicResponseDto(
        String id,
        String image,
        String name,
        int age,
        double weight,
        String history
) {
}
