package com.disney.repository;

import com.disney.model.entity.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class GenreRepositoryTest {
    private final GenreRepository genreRepository;
    private Genre genre;

    @Autowired
    public GenreRepositoryTest(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @BeforeEach
    public void setup() {
        genre = Genre.builder()
                .name("thriller")
                .movies(Collections.emptySet())
                .build();
    }

    @DisplayName(value = "JUnit Test for check if Genre name exists in database and it's true")
    @Test
    public void givenAName_whenCheckIfExists_thenReturnTrue() {
        // given
        final String genreName = "thriller";
        genreRepository.save(genre);

        // when
        boolean result = genreRepository.existsByName(genreName);

        //then
        assertThat(result).isTrue();
    }

    @DisplayName(value = "JUnit Test for check if Genre name exists in database and it's false")
    @Test
    public void givenAName_whenCheckIfExists_thenReturnFalse() {
        // given
        final String genreName = "animation";
        genreRepository.save(genre);

        // when
        boolean result = genreRepository.existsByName(genreName);

        //then
        assertThat(result).isFalse();
    }

    @DisplayName(value = "JUnit Test for successfully Save Genre")
    @Test
    public void givenGenreObject_whenSave_thenReturnSavedGenre() {
        // given
        // when
        Genre savedGenre = genreRepository.save(genre);

        // then
        assertThat(savedGenre).isNotNull();
        assertThat(savedGenre).isInstanceOf(Genre.class);
        assertThat(savedGenre.getId()).isInstanceOf(UUID.class);
        assertThat(savedGenre.getName()).isNotEmpty();
        assertThat(savedGenre.getMovies()).isEmpty();
    }

    @DisplayName(value = "JUnit Test for list all Genres")
    @Test
    public void givenGenreObjects_whenFindAll_thenReturnPaginatedGenreList() {
        // given
        final Genre genre1 = Genre.builder()
                .name("animation")
                .movies(Collections.emptySet())
                .build();
        Genre savedGenre1 = genreRepository.save(genre);
        Genre savedGenre2 = genreRepository.save(genre1);

        // when
        Page<Genre> genres = genreRepository.findAll(PageRequest.of(0, 5));

        // then
        assertThat(genres.getContent()).isNotNull();
        assertThat(genres.getContent()).isNotEmpty().contains(savedGenre1, savedGenre2);
        assertThat(genres.getTotalElements()).isEqualTo(2);
    }

    @DisplayName(value = "JUnit Test for get Genre by ID")
    @Test
    public void givenAnId_whenFindById_thenTheGenreEnIsReturned() {
        // given
        Genre genreSaved = genreRepository.save(genre);

        // when
        final UUID GENRE_ID = genreSaved.getId();
        Optional<Genre> optionalGenre = genreRepository.findById(GENRE_ID);

        //then
        assertThat(optionalGenre.isPresent()).isTrue();
        assertThat(optionalGenre.get()).usingRecursiveComparison().isEqualTo(genreSaved);
    }

    @DisplayName(value = "JUnit Test for update an existent Genre")
    @Test
    public void givenGenreObject_whenUpdateValues_thenReturnUpdatedGenre() {
        // given
        Genre savedGenre = genreRepository.save(genre);
        final UUID GENRE_ID = savedGenre.getId();
        final String NEW_NAME = "suspense";

        // when
        Optional<Genre> optionalSavedGenre = genreRepository.findById(GENRE_ID);
        assertThat(optionalSavedGenre.isPresent()).isTrue();
        optionalSavedGenre.get().setName(NEW_NAME);
        Genre updatedGenre = genreRepository.save(optionalSavedGenre.get());

        //then
        assertThat(updatedGenre.getName()).isEqualTo(NEW_NAME);
    }
}
