package com.disney.unit.repository;

import com.disney.model.entity.Character;
import com.disney.model.entity.Genre;
import com.disney.model.entity.Movie;
import com.disney.repository.CharacterRepository;
import com.disney.repository.GenreRepository;
import com.disney.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MovieRepositoryTest {
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final CharacterRepository characterRepository;

    private Genre genre;
    private Character character;

    @Autowired
    public MovieRepositoryTest(MovieRepository movieRepository,
                               GenreRepository genreRepository,
                               CharacterRepository characterRepository) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.characterRepository = characterRepository;
    }

    @BeforeEach
    void setUp() {
        genre = genreRepository.save(Genre.builder()
                .name("Genre Name")
                .build());
        character = characterRepository.save(Character.builder()
                .image("character-image.jpg")
                .name("Character Name")
                .age(43)
                .weight(90.4)
                .history("Character history")
                .build());
    }

    @DisplayName(value = "JUnit Test for check if Movie title exists in database and result is true")
    @Test
    public void givenMovieTitle_whenCheckIfExists_thenReturnTrue() {
        // given
        final String movieTitle = "Movie Title";
        final Movie movie = Movie.builder()
                .image("movie-image.jpg")
                .title(movieTitle)
                .rate(4)
                .creationDate(LocalDate.now())
                .genre(genre)
                .characters(Set.of(character))
                .build();
        movieRepository.save(movie);

        // when
        boolean result = movieRepository.existsByTitle(movieTitle);

        //then
        assertThat(result).isTrue();
    }

    @DisplayName(value = "JUnit Test for check if Movie title exists in database and result is false")
    @Test
    public void givenMovieTitle_whenCheckIfExists_thenReturnFalse() {
        // given
        final String movieTitle = "other title";
        final Movie movie = Movie.builder()
                .image("movie-image.jpg")
                .title("Movie Title")
                .rate(4)
                .creationDate(LocalDate.now())
                .genre(genre)
                .characters(Set.of(character))
                .build();
        movieRepository.save(movie);

        // when
        boolean result = movieRepository.existsByTitle(movieTitle);

        //then
        assertThat(result).isFalse();
    }
}
