package com.disney.unit.controller;

import com.disney.controller.MovieController;
import com.disney.model.InvalidUUIDFormatException;
import com.disney.model.dto.request.MovieRequestDto;
import com.disney.model.dto.request.MovieUpdateRequestDto;
import com.disney.model.dto.response.ApiErrorResponse;
import com.disney.model.dto.response.MovieResponseDto;
import com.disney.model.dto.response.basic.CharacterBasicResponseDto;
import com.disney.model.dto.response.basic.GenreBasicResponseDto;
import com.disney.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.hamcrest.Matchers;
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

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.disney.model.HttpCodeResponse.*;
import static com.disney.util.ApiUtils.*;
import static java.time.LocalDateTime.now;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MovieController.class)
public class MovieControllerTest {
    private final String URL_TEMPLATE = MOVIE_BASE_URL + MOVIE_URI_VARIABLE;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    @MockBean
    private final MovieService movieService;

    private MovieRequestDto movieRequest;
    private MovieUpdateRequestDto updateRequest;

    @Autowired
    public MovieControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, MovieService movieService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.movieService = movieService;
    }

    @BeforeEach
    void setUp() {
        movieRequest = MovieRequestDto.builder()
                .image("movie-image")
                .title("Movie Title")
                .creationDate("1992/06/21")
                .rate(5)
                .genreId(UUID.randomUUID().toString())
                .charactersId(Set.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .build();
        updateRequest = MovieUpdateRequestDto.builder()
                .image("new-movie-image.png")
                .title("New Movie Title")
                .creationDate("2023/11/31")
                .rate(2)
                .genreId(UUID.randomUUID().toString())
                .charactersToAdd(Set.of(UUID.randomUUID().toString()))
                .charactersToRemove(Set.of(UUID.randomUUID().toString()))
                .build();
    }

    @DisplayName(value = "JUnit Test for successfully create a Movie based on a request")
    @Test
    public void givenRequestDto_whenCreateMovie_thenStatusIsCreatedAndIsSavedInDB() throws Exception {
        // given
        willDoNothing().given(movieService).createMovie(any(MovieRequestDto.class));

        // when
        ResultActions response = mockMvc.perform(post(MOVIE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieRequest)));

        // then verify mock interaction
        then(movieService).should(times(1)).createMovie(movieRequest);
        // then verify response is correct and contains expected data results
        response.andDo(print()).andExpect(status().isCreated());
    }

    @DisplayName(value = "JUnit Test for try to create Movie providing a request with null values")
    @Test
    public void givenRequestWithNullValues_whenTryToCreateMovie_thenStatusIsBadRequest() throws Exception {
        // given
        final MovieRequestDto invalidRequest = MovieRequestDto.builder().build();
        final Map<Integer, String> errors = Map.of(
                1, "The title cant be empty or null",
                2, "The title can't be whitespaces",
                3, "The creation date cant be empty or null",
                4, "The creation date can't be whitespaces",
                5, "Positive values only, the minimum is 1",
                6, "Positive values only, the maximum is 5",
                7, "The genre can't be null",
                8, "must not be empty",
                9, "must not be null"
        );

        // when
        ResultActions response = mockMvc.perform(post(MOVIE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // then verify mock interaction
        then(movieService).shouldHaveNoInteractions();
        // then verify response is correct and contains expected data results
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", anyOf(is(errors.get(1)), is(errors.get(2)))))
                .andExpect(jsonPath("$.creationDate", anyOf(is(errors.get(3)), is(errors.get(4)))))
                .andExpect(jsonPath("$.rate", anyOf(is(errors.get(5)), is(errors.get(6)))))
                .andExpect(jsonPath("$.genreId", is(errors.get(7))))
                .andExpect(jsonPath("$.charactersId", anyOf(is(errors.get(8)), is(errors.get(9)))));
    }

    @DisplayName(value = "JUnit Test for create a Movie providing and invalid Date format")
    @Test
    public void givenRequestWithInvalidDateFormat_whenCreateMovie_thenReturnResponseWithStatusBadRequest() throws Exception {
        // given
        final String invalidDateFormat = "07-12-2021"; // expected and correct format is yyyy/MM/dd
        final MovieRequestDto request = MovieRequestDto.builder()
                .image("movie-image")
                .title("Movie Title")
                .creationDate(invalidDateFormat)
                .rate(5)
                .genreId(UUID.randomUUID().toString())
                .charactersId(Set.of(UUID.randomUUID().toString()))
                .build();
        willThrow(DateTimeParseException.class).given(movieService).createMovie(any(MovieRequestDto.class));

        // when
        ResultActions response = mockMvc.perform(post(MOVIE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then verify mock interactions
        then(movieService).should(times(1)).createMovie(request);
        // then verify response is correct and contains expected data results
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.path", is(STR."uri=\{MOVIE_BASE_URL}")))
                .andExpect(jsonPath("$.errorCode", is(INVALID_DATE_FORMAT.toString())));
    }

    @DisplayName(value = "JUnit Test for crete Movie without providing required body content")
    @Test
    public void givenNoRequestBody_whenTryToCreateMovie_thenReturnResponseWithStatusBadRequest() throws Exception {
        // given
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_REQUIRED_PAYLOAD)
                .path(STR."uri=\{MOVIE_BASE_URL}")
                .message("Required request body is missing")
                .build();

        // when
        ResultActions response = mockMvc.perform(post(MOVIE_BASE_URL));

        // then verify mock interactions
        then(movieService).shouldHaveNoInteractions();
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())));
    }

    @DisplayName(value = "JUnit Test for create Movie but the Movie is already registered")
    @Test
    public void givenRequest_whenTryToCreateDuplicatedMovie_thenStatusIsBadRequest() throws Exception {
        // given
        final String errorMsg = STR."The movie '\{movieRequest.title()}' already exist";
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(DUPLICATED_RESOURCE)
                .path(STR."uri=\{MOVIE_BASE_URL}")
                .message(errorMsg)
                .build();
        willThrow(new EntityExistsException(errorMsg)).given(movieService).createMovie(any(MovieRequestDto.class));

        // when
        ResultActions response = mockMvc.perform(post(MOVIE_BASE_URL).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movieRequest)));

        //then verify mock interactions
        then(movieService).should(times(1)).createMovie(movieRequest);
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for get all Movies (no specification)")
    @Test
    public void givenRequest_whenTryToListMovies_thenReturnListWithAllMovies() throws Exception {
        // given
        final int pageNumber = 0;
        final List<MovieResponseDto> mockList = List.of(mock(MovieResponseDto.class), mock(MovieResponseDto.class));
        final PageRequest pageable = PageRequest.of(pageNumber, ELEMENTS_PER_PAGE); // prefixed elements to 10
        final Page<MovieResponseDto> responseList = new PageImpl<>(mockList, pageable, mockList.size());
        given(movieService.listMovies(pageNumber, null, null, "ASC")).willReturn(responseList);

        // when
        ResultActions response = mockMvc.perform(get(MOVIE_BASE_URL).contentType(APPLICATION_JSON)
                .param("page", String.valueOf(pageNumber)));

        //then verify mock interactions
        then(movieService).should(times(1)).listMovies(pageNumber, null, null, "ASC");
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @DisplayName(value = "JUnit Test for get all Movies that match with a specific title")
    @Test
    public void givenTitleSpecification_whenListMovies_thenReturnListWithAllMoviesThatMatch() throws Exception {
        // given
        final int pageNumber = 0;
        final String movieTitle = "Movie Title";
        final List<MovieResponseDto> movies = List.of(
                MovieResponseDto.builder()
                        .id(UUID.randomUUID().toString())
                        .image("movie-image")
                        .title(movieTitle)
                        .creationDate("2023/12/01")
                        .rate(1)
                        .genre(mock(GenreBasicResponseDto.class))
                        .characters(Set.of(mock(CharacterBasicResponseDto.class)))
                        .build()
        );
        final PageRequest pageable = PageRequest.of(pageNumber, ELEMENTS_PER_PAGE);
        final Page<MovieResponseDto> responseList = new PageImpl<>(movies, pageable, movies.size());
        given(movieService.listMovies(pageNumber, movieTitle, null, "ASC")).willReturn(responseList);

        // when
        ResultActions response = mockMvc.perform(get(MOVIE_BASE_URL).contentType(APPLICATION_JSON)
                .param("page", String.valueOf(pageNumber))
                .param("title", movieTitle));

        //then verify mock interactions
        then(movieService).should(times(1)).listMovies(pageNumber, movieTitle, null, "ASC");
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty", is(false)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @DisplayName(value = "JUnit Test for successfully update a Movie")
    @Test
    public void givenUpdateRequestAndId_whenUpdateMovie_thenReturnMovieUpdated() throws Exception {
        // given
        final String movieId = UUID.randomUUID().toString();
        final MovieResponseDto expectedResponse = new MovieResponseDto(
                movieId,
                updateRequest.image(),
                updateRequest.title(),
                updateRequest.creationDate(),
                updateRequest.rate(),
                mock(GenreBasicResponseDto.class),
                Set.of(mock(CharacterBasicResponseDto.class)));
        given(movieService.updateMovie(anyString(), any(MovieUpdateRequestDto.class))).willReturn(expectedResponse);

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // then verify mock interactions
        then(movieService).should(times(1)).updateMovie(movieId, updateRequest);
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedResponse.id())))
                .andExpect(jsonPath("$.image", is(expectedResponse.image())))
                .andExpect(jsonPath("$.title", is(expectedResponse.title())))
                .andExpect(jsonPath("$.creationDate", is(expectedResponse.creationDate())))
                .andExpect(jsonPath("$.rate", is(expectedResponse.rate())));
    }

    @DisplayName(value = "JUnit Test for update Movie and the Movie to update is not present in the data base")
    @Test
    public void givenRequestAndId_whenTryToUpdateMovieAndIsNotPresent_thenStatusIsNotFound() throws Exception {
        // given
        final String movieId = UUID.randomUUID().toString();
        final String errorMsg = STR."Movie not found for ID \{movieId}";
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(RESOURCE_NOT_FOUND)
                .path(STR."uri=\{MOVIE_BASE_URL}/\{movieId}")
                .message(errorMsg)
                .build();
        given(movieService.updateMovie(anyString(), any(MovieUpdateRequestDto.class)))
                .willThrow(new EntityNotFoundException(errorMsg));

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        //then verify mock interactions
        then(movieService).should(times(1)).updateMovie(movieId, updateRequest);
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for update Movie with invalid ID format as path variable")
    @Test
    public void givenInvalidIdFormat_whenUpdateMovie_thenStatusIsBadRequest() throws Exception {
        // given
        final String invalidIdFormat = "123-456-789";
        final String errorMsg = STR."Invalid UUID string: \{invalidIdFormat}";
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_ID_FORMAT)
                .path(STR."uri=\{MOVIE_BASE_URL}/\{invalidIdFormat}")
                .message(errorMsg)
                .build();
        given(movieService.updateMovie(anyString(), any(MovieUpdateRequestDto.class)))
                .willThrow(new InvalidUUIDFormatException(errorMsg));

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, invalidIdFormat).contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        //then verify mock interactions
        then(movieService).should(times(1)).updateMovie(invalidIdFormat, updateRequest);
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for bad request when update Movie without required body")
    @Test
    public void givenMissingRequestBody_whenTryToUpdateMovie_thenStatusIsBadRequest() throws Exception {
        // given
        final String movieId = UUID.randomUUID().toString();
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_REQUIRED_PAYLOAD)
                .path(STR."uri=\{MOVIE_BASE_URL}/\{movieId}")
                .message("Required request body is missing")
                .build();

        // when
        ResultActions response = mockMvc.perform(patch(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON));

        //then verify mock interactions
        then(movieService).shouldHaveNoInteractions();
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for successfully delete Movie by ID")
    @Test
    public void givenMovieId_whenDeleteMovie_thenMovieIsDeletedAndStatusIsNoContent() throws Exception {
        // given
        final String movieId = UUID.randomUUID().toString();
        willDoNothing().given(movieService).deleteMovie(anyString());

        // when
        ResultActions response = mockMvc.perform(delete(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON));

        //then verify mock interactions
        then(movieService).should(times(1)).deleteMovie(movieId);
        // then verify response is correct and contains expected data
        response.andDo(print()).andExpect(status().isNoContent());
    }

    @DisplayName(value = "JUnit Test for delete Movie with an invalid ID format")
    @Test
    public void givenInvalidIdFormat_whenTryToDeleteMovie_thenServiceThrowsAndStatusIsBadRequest() throws Exception {
        // given
        final String invalidIdFormat = "123-456-789";
        final String errorMsg = STR."Invalid UUID string: \{invalidIdFormat}";
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_ID_FORMAT)
                .path(STR."uri=\{MOVIE_BASE_URL}/\{invalidIdFormat}")
                .message(errorMsg)
                .build();
        willThrow(new InvalidUUIDFormatException(errorMsg)).given(movieService).deleteMovie(anyString());

        // when
        ResultActions response = mockMvc.perform(delete(URL_TEMPLATE, invalidIdFormat).contentType(APPLICATION_JSON));

        //then verify mock interactions
        then(movieService).should(times(1)).deleteMovie(invalidIdFormat);
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for delete Movie but is not present in the database")
    @Test
    public void givenMovieId_whenTryToDeleteMovie_thenServiceThrowsForNotPresentAndStatusIsNotFound() throws Exception {
        // given
        final String movieId = UUID.randomUUID().toString();
        final String errorMsg = STR."Movie not found for ID \{movieId}";
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(RESOURCE_NOT_FOUND)
                .path(STR."uri=\{MOVIE_BASE_URL}/\{movieId}")
                .message(errorMsg)
                .build();
        willThrow(new EntityNotFoundException(errorMsg)).given(movieService).deleteMovie(anyString());

        // when
        ResultActions response = mockMvc.perform(delete(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON));

        //then verify mock interactions
        then(movieService).should(times(1)).deleteMovie(movieId);
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for get Movie by ID")
    @Test
    public void givenMovieId_whenGetMovieById_thenReturnTheMovieAndStatusIsOk() throws Exception {
        // given
        final String movieId = UUID.randomUUID().toString();
        final MovieResponseDto expectedResponse = MovieResponseDto.builder()
                .id(movieId)
                .image("movie-image.jpg")
                .title("Movie Title")
                .creationDate("1994/06/24")
                .rate(5)
                .genre(mock(GenreBasicResponseDto.class))
                .characters(Set.of(mock(CharacterBasicResponseDto.class)))
                .build();
        given(movieService.getMovieById(anyString())).willReturn(expectedResponse);

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON));

        // then verify mock interactions
        then(movieService).should(times(1)).getMovieById(movieId);
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expectedResponse.id())))
                .andExpect(jsonPath("$.image", is(expectedResponse.image())))
                .andExpect(jsonPath("$.title", is(expectedResponse.title())))
                .andExpect(jsonPath("$.creationDate", is(expectedResponse.creationDate())))
                .andExpect(jsonPath("$.rate", is(expectedResponse.rate())));
    }

    @DisplayName(value = "JUnit Test for get Movie providing an invalid ID format")
    @Test
    public void givenInvalidIdFormat_whenTryToGetMovieById_thenStatusIsBadRequest() throws Exception {
        // given
        final String invalidIdFormat = "123-456-7890";
        final String errorMsg = STR."Invalid UUID string: \{invalidIdFormat}";
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(INVALID_ID_FORMAT)
                .path(STR."uri=\{MOVIE_BASE_URL}/\{invalidIdFormat}")
                .message(errorMsg)
                .build();
        given(movieService.getMovieById(anyString())).willThrow(new InvalidUUIDFormatException(errorMsg));

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, invalidIdFormat).contentType(APPLICATION_JSON));

        // then verify mock interactions
        then(movieService).should(times(1)).getMovieById(invalidIdFormat);
        // then verify response is correct and contains expected data
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }

    @DisplayName(value = "JUnit Test for get Movie by ID but is not present in the database")
    @Test
    public void givenMovieId_whenTryToGetMovieById_thenStatusIsNotFound() throws Exception {
        // given
        final String movieId = UUID.randomUUID().toString();
        final String errorMsg = STR."Movie not found for ID \{movieId}";
        final ApiErrorResponse expectedResponse = ApiErrorResponse.builder()
                .timestamp(now())
                .errorCode(RESOURCE_NOT_FOUND)
                .path(STR."uri=\{MOVIE_BASE_URL}/\{movieId}")
                .message(errorMsg)
                .build();
        given(movieService.getMovieById(anyString())).willThrow(new EntityNotFoundException(errorMsg));

        // when
        ResultActions response = mockMvc.perform(get(URL_TEMPLATE, movieId).contentType(APPLICATION_JSON));

        //then
        then(movieService).should(times(1)).getMovieById(movieId);
        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp", is(notNullValue())))
                .andExpect(jsonPath("$.errorCode", is(expectedResponse.errorCode().toString())))
                .andExpect(jsonPath("$.path", is(expectedResponse.path())))
                .andExpect(jsonPath("$.message", is(expectedResponse.message())));
    }
}
