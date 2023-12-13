package com.disney.integration;

import com.disney.model.dto.request.CharacterRequestDto;
import com.disney.model.dto.request.CharacterUpdateRequestDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.*;

import static com.disney.model.HttpCodeResponse.*;
import static com.disney.util.ApiUtils.CHARACTER_BASE_URL;
import static com.disney.util.ApiUtils.CHARACTER_URI_VARIABLE;
import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CharacterControllerIT extends AbstractContainerBaseTest{
    private final String URL_TEMPLATE = CHARACTER_BASE_URL + CHARACTER_URI_VARIABLE;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final CharacterRepository characterRepository;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    @Autowired
    public CharacterControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, CharacterRepository characterRepository,
                                 MovieRepository movieRepository, GenreRepository genreRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.characterRepository = characterRepository;
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
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
    protected Movie createAndSaveMovieToDb(Character character, Genre genre) {
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

    @BeforeEach
    void setUp() {
        characterRepository.deleteAll();
        movieRepository.deleteAll();
    }

    @DisplayName(value = "JUnit Test for successfully create Character without movies to append")
    @Test
    public void givenRequest_whenTryToCreateCharacter_thenStatusCreatedIsReturned() throws Exception {
        // given
        final CharacterRequestDto request = CharacterRequestDto.builder()
                .image("character-image.png")
                .name("Character Name")
                .age(31)
                .weight(92.7)
                .history("Some text for character history.")
                .moviesId(new HashSet<>(emptySet()))
                .build();

        // when
        ResultActions response = mockMvc.perform(post(CHARACTER_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        response.andDo(print()).andExpect(status().isCreated());
    }

    @DisplayName(value = "JUnit Test for successfully create Character and appending it to a movie")
    @Test
    public void givenRequest_whenTryToCreateCharacterAndAppendToAMovie_thenStatusCreatedIsReturned() throws Exception {
        // ...create a Movie first and save it to DB
        Movie movie = createAndSaveMovieToDb(null, createAndSaveGenreToDb());

        // given
        final CharacterRequestDto request = CharacterRequestDto.builder()
                .image("character-image.png")
                .name("Character Name")
                .age(31)
                .weight(92.7)
                .history("Some text for character history.")
                .moviesId(Set.of(movie.getId().toString()))
                .build();

        // when
        ResultActions response = mockMvc.perform(post(CHARACTER_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        response.andDo(print()).andExpect(status().isCreated());
    }

    @DisplayName(value = "JUnit Test for create Character but the Character is already registered")
    @Test
    public void givenRequest_whenTryToCreateDuplicatedCharacter_thenStatusBadRequestIsReturned() throws Exception {
        // ...first lets save a Character before try to create one with same name
        var character = createAndSaveCharacterToDb();

        // given
        final CharacterRequestDto request = CharacterRequestDto.builder()
                .image("character-image.png")
                .name(character.getName()) // setting same name
                .age(31)
                .weight(92.7)
                .history("Some text for character history.")
                .moviesId(new HashSet<>(emptySet()))
                .build();

        // when
        ResultActions response = mockMvc.perform(post(CHARACTER_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(DUPLICATED_RESOURCE.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=\{CHARACTER_BASE_URL}")))
                .andExpect(jsonPath("$.message", is(STR."The character '\{request.name()}' is already registered")));
    }

    @DisplayName(value = "JUnit Test for create Character null or empty required values")
    @Test
    public void givenRequestWithoutRequiredValues_whenCreateCharacter_thenStatusBadRequestIsReturned() throws Exception {
        // ...list of all possible errors to make the assertions
        Map<Integer, String> errors = Map.of(
                1, "The name can't be empty or null", // message for empty name value
                2, "The name can't be whitespaces", // message for blank name value
                3, "The age can't be null", // message for null age value
                4, "Positive numbers only, minimum is 1", // message for negative age value
                5, "The weight can't be null", // message for null weight value
                6, "Positive numbers only, minimum is 1", // message for negative weight value
                7, "The history can't be empty or null", // message for empty history value
                8, "The history can't be whitespaces" // message for blank history value
        );

        // given
        final CharacterRequestDto invalidRequest = CharacterRequestDto.builder().build();

        // when
        ResultActions response = mockMvc.perform(post(CHARACTER_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        //then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", anyOf(is(errors.get(1)), is(errors.get(2)))))
                .andExpect(jsonPath("$.age", anyOf(is(errors.get(3)), is(errors.get(4)))))
                .andExpect(jsonPath("$.weight", anyOf(is(errors.get(5)), is(errors.get(6)))))
                .andExpect(jsonPath("$.history", anyOf(is(errors.get(7)), is(errors.get(8)))));
    }

    @DisplayName(value = "JUnit Test for try to create Character without required body")
    @Test
    public void givenRequestWithoutBody_whenTryToCreateCharacter_thenStatusBadRequestIsReturned() throws Exception {
        // given non request

        // when
        ResultActions response = mockMvc.perform(post(CHARACTER_BASE_URL).contentType(APPLICATION_JSON));

        //then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(INVALID_REQUIRED_PAYLOAD.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=\{CHARACTER_BASE_URL}")))
                .andExpect(jsonPath("$.message", is("Required request body is missing")));
    }

    @DisplayName(value = "JUnit Test for get all Characters (no specification)")
    @Test
    public void givenRequest_whenTryToListCharacters_thenReturnListWithAllCharacters() throws Exception {
        // ...lets create and save two Characters firs
        characterRepository.saveAll(List.of(
                Character.builder()
                        .image("character-one-image.png")
                        .name("Frank")
                        .age(27)
                        .weight(78.5)
                        .history("Some text for character one history.")
                        .build(),
                Character.builder()
                        .image("character-two-image.png")
                        .name("Tom")
                        .age(27)
                        .weight(81.7)
                        .history("Some text for character two history.")
                        .build(),
                Character.builder()
                        .image("character-three-image.png")
                        .name("Frank Shelter")
                        .age(27)
                        .weight(63)
                        .history("Some text for character three history.")
                        .build()
        ));

        // when
        ResultActions response = mockMvc.perform(get(CHARACTER_BASE_URL).contentType(APPLICATION_JSON));

        //then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @DisplayName(value = "JUnit Test for list Characters filter by name and age arranged in descending order")
    @Test
    public void givenRequest_whenTryToListCharacters_thenReturnFilteredCharacterList() throws Exception {
        // ...lets create and save three Characters firs
        characterRepository.saveAll(List.of(
                Character.builder()
                        .image("character-one-image.png")
                        .name("Frank")
                        .age(27)
                        .weight(78.5)
                        .history("Some text for character one history.")
                        .build(),
                Character.builder()
                        .image("character-two-image.png")
                        .name("Tom")
                        .age(27)
                        .weight(81.7)
                        .history("Some text for character two history.")
                        .build(),
                Character.builder()
                        .image("character-three-image.png")
                        .name("Frank Shelter")
                        .age(27)
                        .weight(63)
                        .history("Some text for character three history.")
                        .build()
        ));

        // given
        final String characterName = "Frank";
        final int age = 27;
        final String order = "desc";

        // when
        ResultActions response = mockMvc.perform(get(CHARACTER_BASE_URL).contentType(APPLICATION_JSON)
                .param("name", characterName).param("age", String.valueOf(age)).param("order", order));

        //then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @DisplayName(value = "JUnit Test for successfully update a Character (but only his basic information, no movies)")
    @Test
    public void givenUpdateRequestAndId_whenTryToUpdateCharacter_thenReturnCharacterUpdated() throws Exception {
        // ...lets save a character first
        var character = createAndSaveCharacterToDb();

        // given
        final String characterId = character.getId().toString();
        final CharacterUpdateRequestDto updateRequest = CharacterUpdateRequestDto.builder()
                .image("NEW-IMAGE")
                .name("NEW NAME")
                .age(99)
                .weight(99.9)
                .history("NEW HISTORY")
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        //then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(characterId)))
                .andExpect(jsonPath("$.image", is(updateRequest.image())))
                .andExpect(jsonPath("$.name", is(updateRequest.name())))
                .andExpect(jsonPath("$.age", is(updateRequest.age())))
                .andExpect(jsonPath("$.weight", is(updateRequest.weight())))
                .andExpect(jsonPath("$.history", is(updateRequest.history())))
                .andExpect(jsonPath("$.movies", empty()));
    }

    @DisplayName(value = "JUnit Test for update a Character appending one new Movie without changing any other value)")
    @Test
    public void givenUpdateRequestAndId_whenUpdateCharacter_thenReturnCharacterUpdatedWithMovieAdded() throws Exception {
        // ...lets create movie and character and save them
        var movie = createAndSaveMovieToDb(null, createAndSaveGenreToDb()); // creating movie without characters
        var character = createAndSaveCharacterToDb(); // creating character without movies

        // given
        final String characterId = character.getId().toString();
        final CharacterUpdateRequestDto updateRequest = CharacterUpdateRequestDto.builder()
                .moviesWhereAppears(Set.of(movie.getId().toString()))
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        //then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(characterId)))
                .andExpect(jsonPath("$.movies", hasSize(1)));
    }

    @DisplayName(value = "JUnit Test for update Character removing one Movie without changing any other value)")
    @Test
    public void givenUpdateRequestAndId_whenUpdateCharacter_thenReturnCharacterWithoutTheMovie() throws Exception {
        // ...lets create movie and character and save them
        var character = createAndSaveCharacterToDb();
        var movie = createAndSaveMovieToDb(character, createAndSaveGenreToDb());
        character.getMovies().add(movie); // adding movie to character before removal
        character = characterRepository.save(character); // saving the updated character

        // given
        final String characterId = character.getId().toString();
        final CharacterUpdateRequestDto updateRequest = CharacterUpdateRequestDto.builder()
                .moviesToUnlink(Set.of(movie.getId().toString()))
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        //then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(characterId)))
                .andExpect(jsonPath("$.movies", is(empty())));
    }

    @DisplayName(value = "JUnit Test for update Character but the Character to update is not present in the database")
    @Test
    public void givenRequestAndCharacterId_whenFindCharacterToUpdate_thenReturnNotFound() throws Exception {
        // given
        final String characterId = UUID.randomUUID().toString();
        final CharacterUpdateRequestDto updateRequest = CharacterUpdateRequestDto.builder().build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, characterId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(RESOURCE_NOT_FOUND.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=\{CHARACTER_BASE_URL}/\{characterId}")))
                .andExpect(jsonPath("$.message", is(STR."Character not found for ID \{characterId}")));
    }

    @DisplayName(value = "JUnit Test for get Character by ID")
    @Test
    public void givenCharacterId_whenGetCharacterById_thenReturnBodyWithCharacter() throws Exception {
        // ... let's create a character first
        var character = createAndSaveCharacterToDb();

        // given
        final String characterId = character.getId().toString();

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, characterId));

        // then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(characterId)))
                .andExpect(jsonPath("$.image", is(character.getImage())))
                .andExpect(jsonPath("$.name", is(character.getName())))
                .andExpect(jsonPath("$.age", is(character.getAge())))
                .andExpect(jsonPath("$.weight", is(character.getWeight())))
                .andExpect(jsonPath("$.history", is(character.getHistory())))
                .andExpect(jsonPath("$.movies", is(empty())));
    }

    @DisplayName(value = "JUnit Test for get Character by ID the Character is not present in the database")
    @Test
    public void givenCharacterId_whenTryToGetCharacterById_thenStatusNotFoundIsReturned() throws Exception {
        // given
        final String characterId = UUID.randomUUID().toString();

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, characterId));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(RESOURCE_NOT_FOUND.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=\{CHARACTER_BASE_URL}/\{characterId}")))
                .andExpect(jsonPath("$.message", is(STR."Character not found for ID \{characterId}")));
    }

    @DisplayName(value = "JUnit Test for successfully delete a Character")
    @Test
    public void givenCharacterId_whenDeleteCharacter_thenStatusNoContentIsReturned() throws Exception {
        // ...let's save a character first
        var character = createAndSaveCharacterToDb();

        // given
        final String characterId = character.getId().toString();

        // when
        ResultActions response = mockMvc.perform(delete(URL_TEMPLATE, characterId));

        // then
        response.andDo(print()).andExpect(status().isNoContent());
    }

    @DisplayName(value = "JUnit Test for delete Character but the Character is not present in the database")
    @Test
    public void givenCharacterId_whenTryToFindCharacterToDelete_thenStatusNotFoundIsReturned() throws Exception {
        // given
        final String characterId = UUID.randomUUID().toString();

        // when
        ResultActions response = mockMvc.perform(delete(URL_TEMPLATE, characterId));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(RESOURCE_NOT_FOUND.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=\{CHARACTER_BASE_URL}/\{characterId}")))
                .andExpect(jsonPath("$.message", is(STR."Character not found for ID \{characterId}")));
    }
}
