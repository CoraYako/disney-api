package com.disney.integration;

import com.disney.model.dto.request.MovieRequestDto;
import com.disney.model.dto.request.MovieUpdateRequestDto;
import com.disney.model.entity.Character;
import com.disney.model.entity.Genre;
import com.disney.model.entity.Movie;
import com.disney.repository.CharacterRepository;
import com.disney.repository.GenreRepository;
import com.disney.repository.MovieRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.NonNull;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.*;

import static com.disney.model.HttpCodeResponse.DUPLICATED_RESOURCE;
import static com.disney.model.HttpCodeResponse.RESOURCE_NOT_FOUND;
import static com.disney.util.ApiUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MovieControllerIT extends AbstractContainerBaseTest{
    private final String URL_TEMPLATE = MOVIE_BASE_URL + MOVIE_URI_VARIABLE;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final MovieRepository movieRepository;
    private final CharacterRepository characterRepository;
    private final GenreRepository genreRepository;

    @Autowired
    public MovieControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, MovieRepository movieRepository,
                             CharacterRepository characterRepository, GenreRepository genreRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.movieRepository = movieRepository;
        this.characterRepository = characterRepository;
        this.genreRepository = genreRepository;
    }

    @BeforeEach
    void setUp() {
        movieRepository.deleteAll();
        characterRepository.deleteAll();
    }

    /**
     * Creates a new Genre with basic information, without providing
     * any Movie.
     *
     * @return the created Genre.
     */
    protected Genre createAndSaveGenreToDb() {
        var genre = Genre.builder()
                .name("Genre Name")
                .build();
        return genreRepository.save(genre);
    }

    /**
     * Creates a Movie with Characters and Genre. It can also create a Movie without any Character passing null value
     * as character parameter.
     *
     * @param character it can be {@literal null}.
     * @param genre     must not be {@literal null}.
     * @return the created Movie.
     */
    protected Movie createAndSaveMovieToDb(Character character, @NonNull Genre genre) {
        Set<Character> characters = Objects.nonNull(character) ? new HashSet<>(Set.of(character)) : new HashSet<>();
        var movie = Movie.builder()
                .title("Movie Title")
                .rate(5)
                .image("movie-image.jpg")
                .creationDate(LocalDate.now())
                .genre(genre)
                .characters(characters)
                .build();
        return movieRepository.save(movie);
    }

    /**
     * Creates a new Character with basic information, without providing
     * any Movie, only the information related to Character like:
     * image, name, age, weight and history.
     *
     * @return the created Character.
     */
    protected Character createAndSaveCharacterToDb() {
        var character = Character.builder()
                .image("character-one-image.png")
                .name("Frank")
                .age(27)
                .weight(78.5)
                .history("Some text for character one history.")
                .movies(new HashSet<>())
                .build();
        return characterRepository.save(character);
    }

    @DisplayName(value = "JUnit Test for create Movie successfully")
    @Test
    public void givenRequest_whenCreateMovie_thenStatusCreatedIsReturned() throws Exception {
        // ...let's create a Genre first to use his ID and a Character too
        var genre = createAndSaveGenreToDb();
        var character = createAndSaveCharacterToDb();

        // given
        final MovieRequestDto request = MovieRequestDto.builder()
                .image("movie-image.jpg")
                .title("Movie Title")
                .creationDate("2024/01/24")
                .rate(4)
                .genreId(genre.getId().toString())
                .charactersId(new HashSet<>(Set.of(character.getId().toString())))
                .build();

        // when
        ResultActions response = mockMvc.perform(post(MOVIE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        response.andDo(print()).andExpect(status().isCreated());
        assertThat(movieRepository.existsByTitle(request.title())).isTrue();
    }

    @DisplayName(value = "JUnit Test for create Movie but already exists a Movie with the same title")
    @Test
    public void givenRequest_whenTryCreateMovie_thenStatusBadRequestIsReturned() throws Exception {
        // ...let's create a Movie before try to use the same name to create another one
        var genre = createAndSaveGenreToDb();
        var character = createAndSaveCharacterToDb();
        createAndSaveMovieToDb(character, genre);

        // given
        final MovieRequestDto request = MovieRequestDto.builder()
                .image("movie-image.jpg")
                .title("Movie Title")
                .creationDate("2024/01/24")
                .rate(4)
                .genreId(genre.getId().toString())
                .charactersId(new HashSet<>(Set.of(character.getId().toString())))
                .build();

        // when
        ResultActions response = mockMvc.perform(post(MOVIE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.message", is(STR."The movie '\{request.title()}' already exist")))
                .andExpect(jsonPath("$.path", is(STR."uri=\{MOVIE_BASE_URL}")))
                .andExpect(jsonPath("$.errorCode", is(DUPLICATED_RESOURCE.toString())));
    }

    @DisplayName(value = "JUnit Test for get Movie by ID")
    @Test
    public void givenMovieId_whenGetMovieById_thenReturnMovieAndStatusIsOk() throws Exception {
        // ...create and save a Movie first
        var character = createAndSaveCharacterToDb();
        var genre = createAndSaveGenreToDb();
        var movie = createAndSaveMovieToDb(character, genre);

        // given
        final String movieId = movie.getId().toString();

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, movieId));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(movie.getId().toString())))
                .andExpect(jsonPath("$.image", is(movie.getImage())))
                .andExpect(jsonPath("$.title", is(movie.getTitle())))
                .andExpect(jsonPath("$.creationDate", is(movie.getCreationDate().format(OF_PATTERN))))
                .andExpect(jsonPath("$.rate", is(movie.getRate())))
                .andExpect(jsonPath("$.genre.name", is(movie.getGenre().getName())))
                .andExpect(jsonPath("$.characters[0].id", is(character.getId().toString())))
                .andExpect(jsonPath("$.characters[0].name", is(character.getName())));
    }

    @DisplayName(value = "JUnit Test for get Movie by ID but the Movie is not present in the database")
    @Test
    public void givenMovieId_whenTryGetMovieById_thenStatusBadRequestIsReturned() throws Exception {
        // given
        final String movieId = UUID.randomUUID().toString();

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, movieId));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.message", is(STR."Movie not found for ID \{movieId}")))
                .andExpect(jsonPath("$.path", is(STR."uri=\{MOVIE_BASE_URL}/\{movieId}")))
                .andExpect(jsonPath("$.errorCode", is(RESOURCE_NOT_FOUND.toString())));
    }

    @DisplayName(value = "JUnit Test for list all Movies (no specification)")
    @Test
    public void givenRequest_whenListMovies_thenAllMoviesAndStatusOkIsReturned() throws Exception {
        // given
        movieRepository.saveAll(List.of(
                createAndSaveMovieToDb(createAndSaveCharacterToDb(), createAndSaveGenreToDb()),
                createAndSaveMovieToDb(createAndSaveCharacterToDb(), createAndSaveGenreToDb()),
                createAndSaveMovieToDb(createAndSaveCharacterToDb(), createAndSaveGenreToDb())
        ));

        // when
        ResultActions response = mockMvc.perform(get(MOVIE_BASE_URL));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @DisplayName(value = "JUnit Test for list Movies that match a given title and rate")
    @Test
    public void givenMovieTitleAndRate_whenListMovies_thenFilteredMovieListIsReturned() throws Exception {
        // ...let's create some Movies first
        movieRepository.saveAll(List.of(
                Movie.builder()
                        .title("Star Wars: Episode IV - A New Hope")
                        .rate(5)
                        .image("star-wars.jpg")
                        .creationDate(LocalDate.now())
                        .genre(createAndSaveGenreToDb())
                        .characters(Set.of(createAndSaveCharacterToDb()))
                        .build(),
                Movie.builder()
                        .title("Star Wars: Episode V – The Empire Strikes Back")
                        .rate(5)
                        .image("star-wars.jpg")
                        .creationDate(LocalDate.now())
                        .genre(createAndSaveGenreToDb())
                        .characters(Set.of(createAndSaveCharacterToDb()))
                        .build(),
                Movie.builder()
                        .title("Scary Movie")
                        .rate(5)
                        .image("scary-movie.jpg")
                        .creationDate(LocalDate.now())
                        .genre(createAndSaveGenreToDb())
                        .characters(Set.of(createAndSaveCharacterToDb()))
                        .build()
        ));

        // given
        final String title = "star wars";
        final String rate = "5";

        // when
        ResultActions response = mockMvc.perform(get(MOVIE_BASE_URL).param("title", title).param("rate", rate));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @DisplayName(value = "JUnit Test for update Movie basic info successfully (without changing characters or genre)")
    @Test
    public void givenUpdateRequestAndMovieId_whenUpdateMovie_thenUpdatedMovieAndStatusOkIsReturned() throws Exception {
        // ... let's create a Movie first
        var character = createAndSaveCharacterToDb();
        var genre = createAndSaveGenreToDb();
        var movie = createAndSaveMovieToDb(character, genre);

        // given
        final String movieId = movie.getId().toString();
        final MovieUpdateRequestDto updateRequest = MovieUpdateRequestDto.builder()
                .image("NEW-IMAGE.png")
                .title("NEW TITLE")
                .creationDate("1999/04/21")
                .rate(1)
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then verify that everything changed except genre and characters
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(movie.getId().toString())))
                .andExpect(jsonPath("$.image", is(updateRequest.image())))
                .andExpect(jsonPath("$.title", is(updateRequest.title())))
                .andExpect(jsonPath("$.creationDate", is(updateRequest.creationDate())))
                .andExpect(jsonPath("$.rate", is(updateRequest.rate())))
                .andExpect(jsonPath("$.genre.name", is(movie.getGenre().getName())))
                .andExpect(jsonPath("$.characters", hasSize(1)))
                .andExpect(jsonPath("$.characters[0].name", is(character.getName())));
    }

    @DisplayName(value = "JUnit Test for update Movie by changing the genre only")
    @Test
    public void givenUpdateRequest_whenUpdateMovie_thenMovieWithNewGenreIsReturned() throws Exception {
        // ...let's create a Movie first
        var movie = createAndSaveMovieToDb(createAndSaveCharacterToDb(), createAndSaveGenreToDb());
        // ...then a new Genre
        var newGenre = genreRepository.save(Genre.builder().name("NEW GENRE").build());

        // given
        final String movieId = movie.getId().toString();
        final MovieUpdateRequestDto updateRequest = MovieUpdateRequestDto.builder()
                .genreId(newGenre.getId().toString())
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genre.id", is(updateRequest.genreId())))
                .andExpect(jsonPath("$.genre.name", is(newGenre.getName())));
    }

    @DisplayName(value = "JUnit Test for update Movie by adding a new Character without changing any other value")
    @Test
    public void givenUpdateRequest_whenUpdateMovie_thenUpdatedMovieWithNewCharacterIsReturned() throws Exception {
        // ...let's create a Movie first with only ONE character
        var movie = createAndSaveMovieToDb(createAndSaveCharacterToDb(), createAndSaveGenreToDb());
        // ...a new Character to add to the list of characters of the Movie
        var newCharacter = createAndSaveCharacterToDb();

        // given
        final String movieId = movie.getId().toString();
        final MovieUpdateRequestDto updateRequest = MovieUpdateRequestDto.builder()
                .charactersToAdd(Set.of(newCharacter.getId().toString()))
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characters", hasSize(2)));
    }

    @DisplayName(value = "JUnit Test for update Movie by removing a Character without changing any other value")
    @Test
    public void givenUpdateRequest_whenUpdateMovie_thenUpdatedMovieWithoutCharacterIsReturned() throws Exception {
        // ...let's create a Movie with only ONE character
        var character = createAndSaveCharacterToDb();
        var movie = createAndSaveMovieToDb(character, createAndSaveGenreToDb());

        // given
        final String movieId = movie.getId().toString();
        final MovieUpdateRequestDto updateRequest = MovieUpdateRequestDto.builder()
                .charactersToRemove(Set.of(character.getId().toString()))
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.characters", is(empty())));
    }

    @DisplayName(value = "JUnit Test for update Movie but the Movie to update is not present in the database")
    @Test
    public void givenIdAndUpdateRequest_whenTryToFindMovieToUpdate_thenStatusNotFoundIsReturned() throws Exception {
        // given
        final String movieId = UUID.randomUUID().toString();
        final MovieUpdateRequestDto updateRequest = MovieUpdateRequestDto.builder()
                .title("NEW TITLE")
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.message", is(STR."Movie not found for ID \{movieId}")))
                .andExpect(jsonPath("$.path", is(STR."uri=\{MOVIE_BASE_URL}/\{movieId}")))
                .andExpect(jsonPath("$.errorCode", is(RESOURCE_NOT_FOUND.toString())));
    }

    @DisplayName(value = "JUnit Test for delete Movie by ID successfully")
    @Test
    public void givenMovieId_whenDeleteMovie_thenStatusNoContentIsReturned() throws Exception {
        // ...let's create a Movie first to try to delete it latter
        var movie = createAndSaveMovieToDb(null, createAndSaveGenreToDb());

        // given
        final String movieId = movie.getId().toString();

        // when
        ResultActions response = mockMvc.perform(delete(URL_TEMPLATE, movieId));

        // then
        response.andDo(print()).andExpect(status().isNoContent());
        assertThat(movieRepository.existsByTitle(movie.getTitle())).isFalse();
    }

    @DisplayName(value = "JUnit Test for delete Movie but the Movie to delete is not present in the database")
    @Test
    public void givenMovieId_whenTryToFindTheMovieToDelete_thenStatusNotFoundIsReturned() throws Exception {
        // given
        final String movieId = UUID.randomUUID().toString();

        // when
        ResultActions response = mockMvc.perform(delete(URL_TEMPLATE, movieId));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.message", is(STR."Movie not found for ID \{movieId}")))
                .andExpect(jsonPath("$.path", is(STR."uri=\{MOVIE_BASE_URL}/\{movieId}")))
                .andExpect(jsonPath("$.errorCode", is(RESOURCE_NOT_FOUND.toString())));
    }
}
