package com.disney.controller;

import com.disney.model.HttpCodeResponse;
import com.disney.model.InvalidUUIDFormatException;
import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.request.GenreUpdateRequestDto;
import com.disney.model.dto.response.ApiErrorResponse;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.model.entity.Genre;
import com.disney.service.GenreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static com.disney.model.HttpCodeResponse.*;
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
    private final String URL_TEMPLATE = GENRE_BASE_URL + GENRE_URI_VARIABLE;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final GenreService genreService;

    private GenreUpdateRequestDto updateGenreRequest;

    @Autowired
    public GenreControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, GenreService genreService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.genreService = genreService;
    }

    @BeforeEach
    void setUp() {
        updateGenreRequest = new GenreUpdateRequestDto("New Name");
    }

    @DisplayName(value = "JUnit Test for successfully create Genre")
    @Test
    public void givenRequest_whenCreateGenre_thenStatusIsCreated() throws Exception {
        // given
        final GenreRequestDto request = new GenreRequestDto("Genre Name");
        willDoNothing().given(genreService).createGenre(request);

        // when
        ResultActions response = mockMvc.perform(post(GENRE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        then(genreService).should(times(1)).createGenre(request);
        response.andDo(print()).andExpect(status().isCreated());
    }

    @DisplayName(value = "JUnit Test for create Genre based on a request with null values")
    @Test
    public void givenRequestWithNullValues_whenTryToCreateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        final GenreRequestDto request = new GenreRequestDto(null);
        final String msgNotNull = "The name can't be empty or null";
        final String msgNotBlank = "The name can't be whitespaces";

        // when
        ResultActions response = mockMvc.perform(post(GENRE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        then(genreService).shouldHaveNoInteractions();
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", anyOf(is(msgNotNull), is(msgNotBlank))));

    }

    @DisplayName(value = "JUnit Test for create Genre and there is already a Genre in the database")
    @Test
    public void givenRequest_whenTryToCreateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        final GenreRequestDto request = new GenreRequestDto("Genre Name");
        final String message = "The Genre '%s' is already registered.".formatted(request.name());
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(DUPLICATED_RESOURCE)
                .path("uri=" + GENRE_BASE_URL)
                .message(message)
                .build();

        willThrow(new EntityExistsException(message)).given(genreService).createGenre(any(GenreRequestDto.class));

        // when
        ResultActions response = mockMvc.perform(post(GENRE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        then(genreService).should(times(1)).createGenre(request);
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));

    }

    @DisplayName(value = "JUnit Test for create Genre without required body")
    @Test
    public void givenRequestWithoutBody_whenTryToCreateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
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
        final int pageNumber = 0;
        final List<GenreResponseDto> mockList = List.of(mock(GenreResponseDto.class), mock(GenreResponseDto.class));
        final PageRequest pageable = PageRequest.of(pageNumber, ELEMENTS_PER_PAGE);
        final Page<GenreResponseDto> genreList = new PageImpl<>(mockList, pageable, mockList.size());

        given(genreService.listMovieGenres(pageNumber)).willReturn(genreList);

        // when
        ResultActions response = mockMvc.perform(get(GENRE_BASE_URL).contentType(APPLICATION_JSON)
                .param("page", String.valueOf(pageNumber)));

        //then
        then(genreService).should(times(1)).listMovieGenres(pageNumber);
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is((2))));
    }

    @DisplayName(value = "JUnit Test for successfully get Genre by ID")
    @Test
    public void givenGenreId_whenTryToPerformGetGenreById_thenReturnsTheRelatedGenre() throws Exception {
        // given
        final Genre genre = Genre.builder().id(UUID.randomUUID()).name("Genre Name").movies(emptySet()).build();
        final String genreId = genre.getId().toString();

        given(genreService.getGenreById(genreId)).willReturn(new GenreResponseDto(genreId, genre.getName(), emptySet()));

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, genreId).contentType(APPLICATION_JSON));

        //then
        then(genreService).should(times(1)).getGenreById(genreId);
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(genre.getId().toString())))
                .andExpect(jsonPath("$.name", is(genre.getName())));
    }

    @DisplayName(value = "JUnit Test for get Genre by ID providing a non valid UUID format")
    @Test
    public void givenInvalidIdFormat_whenGetGenreById_thenStatusIsBadRequest() throws Exception {
        // given
        final String invalidUUIDFormat = "invalid_id";
        final String message = "Invalid UUID string: %s".formatted(invalidUUIDFormat);
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_ID_FORMAT)
                .path("uri=/api/v1/genres/" + invalidUUIDFormat)
                .message(message)
                .build();
        given(genreService.getGenreById(anyString())).willThrow(new InvalidUUIDFormatException(message));

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, invalidUUIDFormat)
                .contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(updateGenreRequest)));

        //then
        then(genreService).should(times(1)).getGenreById(invalidUUIDFormat);
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for get Genre by ID and Genre is not present in the database")
    @Test
    public void givenGenreId_whenTryToPerformGetGenreBy_thenStatusIsNotFound() throws Exception {
        // given
        final String genreId = UUID.randomUUID().toString();
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
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
        then(genreService).should(times(1)).getGenreById(genreId);
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for update Genre successfully")
    @Test
    public void givenRequestBodyAndId_whenTryToUpdateGenre_thenReturnTheGenreUpdated() throws Exception {
        // given
        final String genreId = UUID.randomUUID().toString();
        final GenreResponseDto expectedResponse = new GenreResponseDto(genreId, updateGenreRequest.name(), emptySet());

        given(genreService.updateGenre(genreId, updateGenreRequest)).willReturn(expectedResponse);

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, genreId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateGenreRequest)));

        //then
        then(genreService).should(times(1)).updateGenre(genreId, updateGenreRequest);
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedResponse.id())))
                .andExpect(jsonPath("$.name", is(expectedResponse.name())));
    }

    @DisplayName(value = "JUnit Test for update Genre and Genre to update is not present in the database")
    @Test
    public void givenRequestBodyAndId_whenTryToUpdateGenre_thenStatusIsNotFount() throws Exception {
        // given
        final String genreId = UUID.randomUUID().toString();
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(RESOURCE_NOT_FOUND)
                .path("uri=/api/v1/genres/" + genreId)
                .message("Genre not found for ID %s".formatted(genreId))
                .build();

        given(genreService.updateGenre(anyString(), any(GenreUpdateRequestDto.class)))
                .willThrow(new EntityNotFoundException("Genre not found for ID %s".formatted(genreId)));

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, genreId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateGenreRequest)));

        //then
        then(genreService).should(times(1)).updateGenre(genreId, updateGenreRequest);
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for update Genre without required body")
    @Test
    public void givenMissingRequestBody_whenTryToUpdateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        final String genreId = UUID.randomUUID().toString();
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
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

    @DisplayName(value = "JUnit Test for update Genre without providing a valid UUID format")
    @Test
    public void givenInvalidIdProvided_whenTryToUpdateGenre_thenStatusIsBadRequest() throws Exception {
        // given
        final String invalidUUIDFormat = "null";
        final String message = "Invalid UUID string: %s".formatted(invalidUUIDFormat);
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_ID_FORMAT)
                .path("uri=/api/v1/genres/" + invalidUUIDFormat)
                .message(message)
                .build();
        given(genreService.updateGenre(anyString(), any(GenreUpdateRequestDto.class)))
                .willThrow(new InvalidUUIDFormatException(message));

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, invalidUUIDFormat)
                .contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(updateGenreRequest)));

        //then
        then(genreService).should(times(1)).updateGenre(invalidUUIDFormat, updateGenreRequest);
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }
}
