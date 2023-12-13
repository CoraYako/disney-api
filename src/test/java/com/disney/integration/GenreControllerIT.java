package com.disney.integration;

import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.request.GenreUpdateRequestDto;
import com.disney.model.entity.Genre;
import com.disney.repository.GenreRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.disney.model.HttpCodeResponse.*;
import static com.disney.util.ApiUtils.GENRE_BASE_URL;
import static com.disney.util.ApiUtils.GENRE_URI_VARIABLE;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class GenreControllerIT extends AbstractContainerBaseTest {
    private final String URL_TEMPLATE = GENRE_BASE_URL + GENRE_URI_VARIABLE;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final GenreRepository genreRepository;

    @Autowired
    public GenreControllerIT(MockMvc mockMvc, ObjectMapper objectMapper,
                             GenreRepository genreRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.genreRepository = genreRepository;
    }

    @BeforeEach
    void setUp() {
        genreRepository.deleteAll();
    }

    @DisplayName(value = "JUnit Test for successfully create Genre")
    @Test
    public void givenRequest_whenCreateGenre_thenStatusIsCreated() throws Exception {
        // given
        final GenreRequestDto request = new GenreRequestDto("Genre Name");

        // when
        ResultActions response = mockMvc.perform(post(GENRE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        response.andDo(print()).andExpect(status().isCreated());
    }

    @DisplayName(value = "JUnit Test for create Genre but it was already created")
    @Test
    public void givenRequest_whenTryToCreateGenre_thenStatusIsBadRequest() throws Exception {
        // ...first create and save a genre
        genreRepository.save(Genre.builder().name("Genre Name").build());

        // given
        final GenreRequestDto request = new GenreRequestDto("Genre Name");

        // when
        ResultActions response = mockMvc.perform(post(GENRE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(DUPLICATED_RESOURCE.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=\{GENRE_BASE_URL}")))
                .andExpect(jsonPath("$.message", is(STR."The Genre '\{request.name()}' is already registered.")));
    }

    @DisplayName(value = "JUnit Test for create Genre based on a request with null values")
    @Test
    public void givenRequestWithNullValues_whenTryToCreateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        final GenreRequestDto request = new GenreRequestDto(null);
        final Map<Integer, String> errors = Map.of(
                1, "The name can't be empty or null",
                2, "The name can't be whitespaces"
        );

        // when
        ResultActions response = mockMvc.perform(post(GENRE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", anyOf(is(errors.get(1)), is(errors.get(2)))));
    }

    @DisplayName(value = "JUnit Test for list all Genres providing page number")
    @Test
    public void givenPageNumber_whenTryToListAllGenres_thenReturnAListWithAllGenres() throws Exception {
        // ...save some objects to fetch it when perform API call
        genreRepository.saveAll(List.of(
                Genre.builder().name("Genre One").build(),
                Genre.builder().name("Genre Two").build()));

        // given
        final int pageNumber = 0;

        // when
        ResultActions response = mockMvc.perform(get(GENRE_BASE_URL).param("page", String.valueOf(pageNumber)));

        //then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is((2))));
    }

    @DisplayName(value = "JUnit Test for list all Genres without providing any page number")
    @Test
    public void givenRequest_whenTryToListAllGenres_thenReturnAListWithAllGenres() throws Exception {
        // ...save some objects to fetch it when perform API call
        genreRepository.saveAll(List.of(
                Genre.builder().name("Genre One").build(),
                Genre.builder().name("Genre Two").build()));

        // given no page number value

        // when fetch all genres (the default value for required pageNumber is 0)
        ResultActions response = mockMvc.perform(get(GENRE_BASE_URL));

        //then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is((2))));
    }

    @DisplayName(value = "JUnit Test for successfully get Genre by ID")
    @Test
    public void givenGenreId_whenTryToGetGenreById_thenReturnsTheRelatedGenre() throws Exception {
        // ...first save a Genre
        var genre = genreRepository.save(Genre.builder().name("Genre Name").build());

        // given
        final String genreId = genre.getId().toString();

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, genreId));

        //then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(genre.getId().toString())))
                .andExpect(jsonPath("$.name", is(genre.getName())))
                .andExpect(jsonPath("$.movies", empty()));
    }

    @DisplayName(value = "JUnit Test for get Genre by ID but the Genre is not present in the database")
    @Test
    public void givenGenreId_whenGetGenreById_thenStatusIsNotFound() throws Exception {
        // given
        final String genreId = UUID.randomUUID().toString();

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, genreId));

        //then
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is(RESOURCE_NOT_FOUND.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=/api/v1/genres/\{genreId}")))
                .andExpect(jsonPath("$.message", is(STR."Genre not found for ID \{genreId}")));
    }

    @DisplayName(value = "JUnit Test for get Genre by ID providing an invalid UUID format")
    @Test
    public void givenInvalidIdFormat_whenGetGenreById_thenStatusIsBadRequest() throws Exception {
        // given
        final String invalidUUIDFormat = "123-456-789-0abc";

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, invalidUUIDFormat));

        //then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(INVALID_ID_FORMAT.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=/api/v1/genres/\{invalidUUIDFormat}")))
                .andExpect(jsonPath("$.message", is("Invalid UUID string: 123-456-789-0abc")));
    }

    @DisplayName(value = "JUnit Test for update Genre successfully")
    @Test
    public void givenRequestBodyAndId_whenUpdateGenre_thenReturnTheGenreUpdated() throws Exception {
        // ...lets save a genre first
        var genre = genreRepository.save(Genre.builder().name("Genre Name").build());

        // given
        final String genreId = genre.getId().toString();
        final GenreUpdateRequestDto updateRequest = new GenreUpdateRequestDto("NEW NAME");

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, genreId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        //then
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(genreId)))
                .andExpect(jsonPath("$.name", is(updateRequest.name())))
                .andExpect(jsonPath("$.movies", empty()));
    }

    @DisplayName(value = "JUnit Test for update Genre but Genre to update is not present in the database")
    @Test
    public void givenRequestBodyAndId_whenTryToUpdateGenre_thenStatusIsNotFount() throws Exception {
        // given
        final String genreId = UUID.randomUUID().toString();
        final GenreUpdateRequestDto updateRequest = new GenreUpdateRequestDto("NEW NAME");

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, genreId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        //then
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is(RESOURCE_NOT_FOUND.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=/api/v1/genres/\{genreId}")))
                .andExpect(jsonPath("$.message", is(STR."Genre not found for ID \{genreId}")));
    }

    @DisplayName(value = "JUnit Test for update Genre without required body")
    @Test
    public void givenMissingRequestBody_whenTryToUpdateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        final String genreId = UUID.randomUUID().toString();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, genreId).contentType(APPLICATION_JSON));

        //then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(INVALID_REQUIRED_PAYLOAD.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=/api/v1/genres/\{genreId}")))
                .andExpect(jsonPath("$.message", is("Required request body is missing")));
    }

    @DisplayName(value = "JUnit Test for update Genre providing an invalid UUID format")
    @Test
    public void givenInvalidIdFormat_whenTryToUpdateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        final String invalidUUIDFormat = "123-456-789";
        final GenreUpdateRequestDto updateRequest = new GenreUpdateRequestDto("NEW NAME");

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, invalidUUIDFormat).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        //then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(INVALID_ID_FORMAT.toString())))
                .andExpect(jsonPath("$.path", is(STR."uri=/api/v1/genres/\{invalidUUIDFormat}")))
                .andExpect(jsonPath("$.message", is(STR."Invalid UUID string: \{invalidUUIDFormat}")));
    }
}
