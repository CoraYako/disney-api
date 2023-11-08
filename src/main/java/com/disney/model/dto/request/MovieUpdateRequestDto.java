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
}
