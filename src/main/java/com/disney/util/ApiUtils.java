package com.disney.util;

import com.disney.model.InvalidUUIDFormatException;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class ApiUtils {
    public static final DateTimeFormatter OF_PATTERN = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public static final String GENRE_BASE_URL = "/api/v1/genres";
    public static final String GENRE_URI_VARIABLE = "/{genreId}";
    public static final String CHARACTER_BASE_URL = "/api/v1/characters";
    public static final String CHARACTER_URI_VARIABLE = "/{characterId}";
    public static final int ELEMENTS_PER_PAGE = 10;

    public static boolean isASC(String order) {
        return order.compareToIgnoreCase("ASC") == 0;
    }

    public static UUID getUUIDFromString(String value) {
        try {
            return UUID.fromString(Objects.requireNonNull(value));
        } catch (Exception e) {
            throw new InvalidUUIDFormatException(e.getMessage());
        }
    }
}
