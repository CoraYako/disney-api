package com.disney.controller;

import com.disney.model.HttpCodeResponse;
import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.request.GenreUpdateRequestDto;
import com.disney.model.dto.response.ApiErrorResponse;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.model.entity.Genre;
import com.disney.service.GenreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static com.disney.model.HttpCodeResponse.INVALID_REQUIRED_PAYLOAD;
import static com.disney.util.ApiUtils.*;
import static java.time.LocalDateTime.now;
import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GenreController.class)
public class GenreControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private GenreService genreService;
    private final String URL_TEMPLATE = GENRE_BASE_URL + GENRE_URI_VARIABLE;

    @DisplayName(value = "JUnit Test for create Genre based on a valid request")
    @Test
    public void givenRequest_whenCreateGenre_thenStatusIsCreated() throws Exception {
        // given
        GenreRequestDto request = new GenreRequestDto("Genre Name");
        willDoNothing().given(genreService).createGenre(request);

        // when
        ResultActions response = mockMvc.perform(post(GENRE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        then(genreService).should(times(INVOKED_ONE_TIME)).createGenre(request);
        response.andDo(print()).andExpect(status().isCreated());
    }

    @DisplayName(value = "JUnit Test for bad request when try to create Genre based on a invalid request")
    @Test
    public void givenInvalidRequest_whenTryToCreateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        GenreRequestDto request = new GenreRequestDto(null);
        String msgNotNull = "The name can't be empty or null";
        String msgNotBlank = "The name can't be whitespaces";

        // when
        ResultActions response = mockMvc.perform(post(GENRE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        then(genreService).shouldHaveNoInteractions();
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", anyOf(is(msgNotNull), is(msgNotBlank))));

    }

    @DisplayName(value = "JUnit Test for bad request when try to create Genre without required body")
    @Test
    public void givenRequestWithoutBody_whenTryToCreateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_REQUIRED_PAYLOAD)
                .path("uri=" + GENRE_BASE_URL)
                .message("Required request body is missing")
                .build();

        // when
        ResultActions response = mockMvc.perform(post(GENRE_BASE_URL).contentType(APPLICATION_JSON));

        //then
        then(genreService).shouldHaveNoInteractions();
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));

    }

    @DisplayName(value = "JUnit Test for get all Genres")
    @Test
    public void givenRequest_whenTryToListAllGenres_thenReturnAListWithAllGenres() throws Exception {
        // given
        int pageNumber = 0;
        Page<GenreResponseDto> genreList = new PageImpl<>(List.of(
                new GenreResponseDto(UUID.randomUUID().toString(), "Genre One", emptySet()),
                new GenreResponseDto(UUID.randomUUID().toString(), "Genre Two", emptySet())));
        given(genreService.listMovieGenres(pageNumber)).willReturn(genreList);

        // when
        ResultActions response = mockMvc.perform(get(GENRE_BASE_URL).contentType(APPLICATION_JSON)
                .param("page", "0"));

        //then
        then(genreService).should(times(INVOKED_ONE_TIME)).listMovieGenres(pageNumber);
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(((int) genreList.getTotalElements()))))
                .andExpect(jsonPath("$.totalPages", is(genreList.getTotalPages())));
    }

    @DisplayName(value = "JUnit Test for get genre by ID")
    @Test
    public void givenGenreId_whenTryToPerformGetGenreById_thenReturnsTheRelatedGenre() throws Exception {
        // given
        Genre genre = Genre.builder().id(UUID.randomUUID()).name("Genre Name").movies(emptySet()).build();
        final String genreId = genre.getId().toString();

        given(genreService.getGenreById(genreId)).willReturn(new GenreResponseDto(genreId, genre.getName(), emptySet()));

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, genreId).contentType(APPLICATION_JSON));

        //then
        then(genreService).should(times(INVOKED_ONE_TIME)).getGenreById(genreId);
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(genre.getId().toString())))
                .andExpect(jsonPath("$.name", is(genre.getName())));
    }

    @DisplayName(value = "JUnit Test for not found when try to get Genre by ID")
    @Test
    public void givenGenreId_whenTryToPerformGetGenreBy_thenStatusIsNotFound() throws Exception {
        // given
        String genreId = UUID.randomUUID().toString();
        ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(HttpCodeResponse.RESOURCE_NOT_FOUND)
                .path("uri=/api/v1/genres/" + genreId)
                .message("Genre not found for ID %s".formatted(genreId))
                .build();
        given(genreService.getGenreById(genreId))
                .willThrow(new EntityNotFoundException("Genre not found for ID %s".formatted(genreId)));

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, genreId).contentType(APPLICATION_JSON));

        //then
        then(genreService).should(times(INVOKED_ONE_TIME)).getGenreById(genreId);
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for successfully updates a Genre")
    @Test
    public void givenRequestBodyAndId_whenTryToUpdateGenre_thenReturnTheGenreUpdated() throws Exception {
        // given
        String genreId = UUID.randomUUID().toString();
        GenreUpdateRequestDto updateGenreRequest = new GenreUpdateRequestDto("New Name");
        GenreResponseDto expectedResponse = new GenreResponseDto(genreId, updateGenreRequest.name(), emptySet());

        given(genreService.updateGenre(genreId, updateGenreRequest)).willReturn(expectedResponse);

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, genreId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateGenreRequest)));

        //then
        then(genreService).should(times(INVOKED_ONE_TIME)).updateGenre(genreId, updateGenreRequest);
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedResponse.id())))
                .andExpect(jsonPath("$.name", is(expectedResponse.name())));
    }

    @DisplayName(value = "JUnit Test for bad request when try to update a Genre without required body")
    @Test
    public void givenMissingRequestBody_whenTryToUpdateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        String genreId = UUID.randomUUID().toString();
        ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_REQUIRED_PAYLOAD)
                .path("uri=/api/v1/genres/" + genreId)
                .message("Required request body is missing")
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, genreId).contentType(APPLICATION_JSON));

        //then
        then(genreService).shouldHaveNoInteractions();
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }
}
