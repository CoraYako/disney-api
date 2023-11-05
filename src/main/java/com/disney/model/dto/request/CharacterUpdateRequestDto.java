package com.disney.model.dto.request;

import java.util.Set;

public record CharacterUpdateRequestDto(
        String image,
        String name,
        int age,
        double weight,
        String history,
        Set<String> moviesId
) {
}
