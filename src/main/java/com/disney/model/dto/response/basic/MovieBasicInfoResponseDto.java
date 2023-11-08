package com.disney.model.dto.response.basic;

public record MovieBasicInfoResponseDto(
        String id,
        String image,
        String title,
        String creationDate,
        int rate,
        GenreBasicResponseDto genre
) {
}
