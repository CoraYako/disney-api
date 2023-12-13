package com.disney.unit.service;

import com.disney.model.InvalidUUIDFormatException;
import com.disney.model.dto.request.MovieRequestDto;
import com.disney.model.dto.request.MovieUpdateRequestDto;
import com.disney.model.dto.response.MovieResponseDto;
import com.disney.model.dto.response.basic.CharacterBasicResponseDto;
import com.disney.model.dto.response.basic.GenreBasicResponseDto;
import com.disney.model.entity.Character;
import com.disney.model.entity.Genre;
import com.disney.model.entity.Movie;
import com.disney.model.mapper.MovieMapper;
import com.disney.repository.MovieRepository;
import com.disney.repository.specification.MovieSpecification;
import com.disney.service.CharacterService;
import com.disney.service.GenreService;
import com.disney.service.implement.MovieServiceImpl;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.*;

import static com.disney.util.ApiUtils.ELEMENTS_PER_PAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieMapper movieMapper;
    @Mock
    private MovieSpecification movieSpec;
    @Mock
    private CharacterService characterService;
    @Mock
    private GenreService genreService;
    @InjectMocks
    private MovieServiceImpl movieService;
    @Captor
    private ArgumentCaptor<Movie> movieArgumentCaptor;

    private MovieRequestDto movieRequest;
    private Movie movie;
    private MovieResponseDto movieResponse;

    @BeforeEach
    void setUp() {
        movieRequest = new MovieRequestDto(
                "movie-image.jpg",
                "Movie Title",
                "1995/04/13",
                4,
                UUID.randomUUID().toString(), // genre id
                Set.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()) // characters id
        );
        movie = Movie.builder()
                .id(UUID.randomUUID())
                .image("movie-image.jpg")
                .title("Movie Title")
                .rate(4)
                .creationDate(LocalDate.now())
                .genre(mock(Genre.class))
                .characters(new HashSet<>(Set.of(mock(Character.class))))
                .build();
        movieResponse = new MovieResponseDto(
                movie.getId().toString(),
                movie.getImage(),
                movie.getTitle(),
                movie.getCreationDate().toString(),
                movie.getRate(),
                mock(GenreBasicResponseDto.class, "genre"),
                Set.of(mock(CharacterBasicResponseDto.class, "characters"))
        );
    }

    @DisplayName(value = "JUnit Test for successfully create a Movie")
    @Test
    public void givenRequestDto_whenCreateMovie_thenMovieIsSavedInDatabase() {
        // given
        given(movieRepository.existsByTitle(anyString())).willReturn(false);
        given(movieMapper.toEntity(any(MovieRequestDto.class))).willReturn(movie);
        given(genreService.getGenreById(any(UUID.class))).willReturn(Genre.builder().build());
        given(characterService.getCharacterById(any(UUID.class))).willReturn(Character.builder().build());
        given(movieRepository.save(any(Movie.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        movieService.createMovie(movieRequest);

        //then
        then(movieRepository).should(times(1)).existsByTitle(movieRequest.title());
        then(movieMapper).should(times(1)).toEntity(movieRequest);
        then(genreService).should(times(1)).getGenreById(any(UUID.class));
        then(characterService).should(times(2)).getCharacterById(any(UUID.class));
        then(movieRepository).should(times(1)).save(movieArgumentCaptor.capture());
        assertThat(movieArgumentCaptor.getValue()).isNotNull().usingRecursiveComparison().isEqualTo(movie);
    }

    @DisplayName(value = "JUnit Test for create Movie when request DTO is null")
    @Test
    public void givenNullRequestDto_whenTryToCreateMovie_thenThrowsInvalidParameterException() {
        // given
        final String errorMsg = "Invalid parameter value: movie";

        // when
        Throwable result = catchThrowable(() -> movieService.createMovie(null));

        //then
        then(movieMapper).shouldHaveNoInteractions();
        then(movieRepository).shouldHaveNoInteractions();
        then(genreService).shouldHaveNoInteractions();
        then(characterService).shouldHaveNoInteractions();
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for create Movie when required field 'title' is empty")
    @Test
    public void givenRequestDtoWithEmptyTitleValue_whenTryToCreateMovie_thenThrowsInvalidParameterException() {
        // given
        final String emptyMovieTitle = "";
        final MovieRequestDto movieRequest = new MovieRequestDto(
                "movie-image.jpg",
                emptyMovieTitle,
                "1995/04/13",
                4,
                UUID.randomUUID().toString(), // genre id
                Set.of(UUID.randomUUID().toString()) // characters id
        );
        final String errorMsg = "Invalid parameter value: movie";

        // when
        Throwable result = catchThrowable(() -> movieService.createMovie(movieRequest));

        //then
        then(movieMapper).shouldHaveNoInteractions();
        then(movieRepository).shouldHaveNoInteractions();
        then(genreService).shouldHaveNoInteractions();
        then(characterService).shouldHaveNoInteractions();
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for create Movie but there is already a movie in the database with same title")
    @Test
    public void givenRequestDto_whenCreateMovieAndThereIsAMovieWithSameTitle_thenThrowsEntityExistsException() {
        // given
        final String errorMsg = STR."The movie '\{movieRequest.title()}' already exist";
        given(movieRepository.existsByTitle(anyString())).willReturn(true);

        // when
        Throwable result = catchThrowable(() -> movieService.createMovie(movieRequest));

        //then
        then(movieMapper).shouldHaveNoInteractions();
        then(movieRepository).should(times(1)).existsByTitle(movieRequest.title());
        then(genreService).shouldHaveNoInteractions();
        then(characterService).shouldHaveNoInteractions();
        assertThat(result).isNotNull().isInstanceOf(EntityExistsException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for successfully update a Movie without modifying Genre and Characters")
    @Test
    public void givenUpdateRequestAndMovieId_whenUpdateMovie_thenReturnTheMovieUpdated() {
        // given
        final String movieId = movie.getId().toString();
        final MovieUpdateRequestDto updateRequest = new MovieUpdateRequestDto(
                "new-image.jpg",
                "Net Title",
                "1998/07/19",
                5,
                null, // genre id
                null, // characters to add
                null // characters to remove
        );
        final MovieResponseDto movieResponse = new MovieResponseDto(
                movieId,
                updateRequest.image(),
                updateRequest.title(),
                updateRequest.creationDate(),
                updateRequest.rate(),
                mock(GenreBasicResponseDto.class),
                Set.of(mock(CharacterBasicResponseDto.class), mock(CharacterBasicResponseDto.class))
        );
        given(movieRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(movie));
        given(movieRepository.save(any(Movie.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(movieMapper.toDTO(any(Movie.class))).willReturn(movieResponse);

        // when
        MovieResponseDto result = movieService.updateMovie(movieId, updateRequest);

        // then verify mocks interactions
        then(movieRepository).should(times(1)).findById(any(UUID.class));
        then(movieRepository).should(times(1)).save(movie);
        then(movieMapper).should(times(1)).toDTO(movie);
        // then verify movie fields where changed correctly
        assertThat(movie.getImage()).isEqualTo(updateRequest.image());
        assertThat(movie.getTitle()).isEqualTo(updateRequest.title());
        assertThat(movie.getCreationDate()).isEqualTo(LocalDate.of(1998, 7, 19));
        assertThat(movie.getRate()).isEqualTo(updateRequest.rate());
        // then verify result is equal to expected result
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(movieResponse);
    }

    @DisplayName(value = "JUnit Test for update only Movie genre without changing any other value")
    @Test
    public void givenUpdateRequest_whenUpdateMovie_thenReturnMovieUpdatedWithNewGenre() {
        // given
        final Genre genre = Genre.builder().id(UUID.randomUUID()).name("New Genre").build();
        final String movieId = movie.getId().toString();
        final MovieUpdateRequestDto updateRequest = new MovieUpdateRequestDto(
                null, // image
                null, // title
                null, // date
                0, // rate
                genre.getId().toString(), // genre id
                null, // characters to add
                null // characters to remove
        );
        final MovieResponseDto movieResponse = new MovieResponseDto(
                movieId,
                movie.getImage(),
                movie.getTitle(),
                movie.getCreationDate().toString(),
                movie.getRate(),
                new GenreBasicResponseDto(genre.getId().toString(), genre.getName()),
                Set.of(mock(CharacterBasicResponseDto.class), mock(CharacterBasicResponseDto.class))
        );
        given(movieRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(movie));
        given(genreService.getGenreById(any(UUID.class))).willReturn(genre);
        given(movieRepository.save(any(Movie.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(movieMapper.toDTO(any(Movie.class))).willReturn(movieResponse);

        // when
        MovieResponseDto result = movieService.updateMovie(movieId, updateRequest);

        // then verify mocks interactions
        then(movieRepository).should(times(1)).findById(UUID.fromString(movieId));
        then(genreService).should(times(1)).getGenreById(genre.getId());
        then(movieRepository).should(times(1)).save(movie);
        then(movieMapper).should(times(1)).toDTO(movie);
        then(characterService).shouldHaveNoInteractions();
        // then verify that movie genre where correctly changed
        assertThat(movie.getGenre()).isNotNull().isEqualTo(genre);
        // then verify result is equal to expected result
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(movieResponse);
    }

    @DisplayName(value = "JUnit Test for update Movie by adding new characters without changing any other value")
    @Test
    public void givenRequestDtoWithCharactersToAppend_whenUpdateMovie_thenReturnTheUpdatedMovie() {
        // given
        final Character character = Character.builder() // character to add to the movie
                .id(UUID.randomUUID())
                .image("character-image.jpg")
                .name("Character Name")
                .age(31)
                .weight(93.7)
                .history("Character history")
                .movies(new HashSet<>())
                .build();
        final String movieId = movie.getId().toString();
        final MovieUpdateRequestDto updateRequest = new MovieUpdateRequestDto(
                null, // no image
                null, // no title
                null, // no date
                0, // no rate
                null, // no genre id
                Set.of(character.getId().toString()), // characters to add (just one in this case)
                null // no characters to remove
        );
        final MovieResponseDto movieResponse = new MovieResponseDto(
                movieId,
                movie.getImage(),
                movie.getTitle(),
                movie.getCreationDate().toString(),
                movie.getRate(),
                mock(GenreBasicResponseDto.class),
                Set.of(mock(CharacterBasicResponseDto.class), // set of two characters as response (old and new)
                        new CharacterBasicResponseDto(
                                character.getId().toString(),
                                character.getImage(),
                                character.getName(),
                                character.getAge(),
                                character.getWeight(),
                                character.getHistory())));
        given(movieRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(movie));
        given(characterService.getCharacterById(any(UUID.class))).willReturn(character);
        given(movieRepository.save(any(Movie.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(movieMapper.toDTO(any(Movie.class))).willReturn(movieResponse);

        // when
        MovieResponseDto result = movieService.updateMovie(movieId, updateRequest);

        // then verify mocks interactions
        then(movieRepository).should(times(1)).findById(movie.getId());
        then(characterService).should(times(1)).getCharacterById(character.getId());
        then(movieRepository).should(times(1)).save(movie);
        then(movieMapper).should(times(1)).toDTO(movie);
        // then verify that the correct character was added to the list
        assertThat(movie.getCharacters()).isNotEmpty().contains(character);
        // then verify result is equal to expected result
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(movieResponse);
    }

    @DisplayName(value = "JUnit Test for update Movie by removing a character without changing any other value")
    @Test
    public void givenRequestDtoWithCharacterToRemove_whenUpdateMovie_thenReturnTheUpdatedMovie() {
        // ...create a character to append to movie before try to remove it
        final Character character = Character.builder()
                .id(UUID.randomUUID())
                .build();
        movie.addCharacterToList(character);

        // given
        final String movieId = movie.getId().toString();
        final MovieUpdateRequestDto updateRequest = new MovieUpdateRequestDto(
                null, // no image
                null, // no title
                null, // no date
                0, // no rate
                null, // no genre id
                null, // no characters to add
                Set.of(character.getId().toString()) // characters to remove (just one in this case)
        );
        final MovieResponseDto movieResponse = new MovieResponseDto(
                movieId,
                movie.getImage(),
                movie.getTitle(),
                movie.getCreationDate().toString(),
                movie.getRate(),
                mock(GenreBasicResponseDto.class),
                Set.of(mock(CharacterBasicResponseDto.class)));
        given(movieRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(movie));
        given(characterService.getCharacterById(any(UUID.class))).willReturn(character);
        given(movieRepository.save(any(Movie.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(movieMapper.toDTO(any(Movie.class))).willReturn(movieResponse);

        // when
        MovieResponseDto result = movieService.updateMovie(movieId, updateRequest);

        // then verify mocks interactions
        then(movieRepository).should(times(1)).findById(movie.getId());
        then(characterService).should(times(1)).getCharacterById(character.getId());
        then(movieRepository).should(times(1)).save(movie);
        then(movieMapper).should(times(1)).toDTO(movie);
        // then verify that the correct character was added to the list
        assertThat(movie.getCharacters()).isNotEmpty().doesNotContain(character);
        // then verify result is equal to expected result
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(movieResponse);
    }

    @DisplayName(value = "JUnit Test for update Movie and try to find it with an invalid ID format")
    @Test
    public void givenInvalidIdFormat_whenTryToFindMovieToUpdate_thenThrows() {
        // given
        final String invalidIdFormat = "123-432-765";
        final MovieUpdateRequestDto updateRequest = mock(MovieUpdateRequestDto.class);
        final String errorMsg = STR."Invalid UUID string: \{invalidIdFormat}";

        // when
        Throwable result = catchThrowable(() -> movieService.updateMovie(invalidIdFormat, updateRequest));

        // then verify mocks interactions
        then(movieRepository).shouldHaveNoInteractions();
        then(movieMapper).shouldHaveNoInteractions();
        then(genreService).shouldHaveNoInteractions();
        then(characterService).shouldHaveNoInteractions();
        // then verify result is correct and has the expected data
        assertThat(result).isNotNull().isInstanceOf(InvalidUUIDFormatException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for update Movie but the movie to update is not present in the database")
    @Test
    public void givenUpdateRequestAndId_whenTryToFindTheMovieToUpdate_thenThrowsEntityNotFound() {
        // given
        final String movieId = UUID.randomUUID().toString();
        final MovieUpdateRequestDto updateRequest = mock(MovieUpdateRequestDto.class);
        final String errorMsg = STR."Movie not found for ID \{movieId}";

        // when
        Throwable result = catchThrowable(() -> movieService.updateMovie(movieId, updateRequest));

        //then verify mocks interactions
        then(movieRepository).should(times(1)).findById(UUID.fromString(movieId));
        then(movieRepository).shouldHaveNoMoreInteractions();
        then(movieMapper).shouldHaveNoInteractions();
        then(genreService).shouldHaveNoInteractions();
        then(characterService).shouldHaveNoInteractions();
        // then verify result is correct and has the expected data
        assertThat(result).isNotNull().isInstanceOf(EntityNotFoundException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for update Movie providing a null ID")
    @Test
    public void givenNullId_whenTryToUpdateMovie_thenThrowsInvalidParameter() {
        // given
        final MovieUpdateRequestDto updateRequestDto = mock(MovieUpdateRequestDto.class);
        final String errorMsg = "Invalid argument passed: movie Id";

        // when
        Throwable result = catchThrowable(() -> movieService.updateMovie(null, updateRequestDto));

        // then verify mocks interactions
        then(movieRepository).shouldHaveNoInteractions();
        then(movieMapper).shouldHaveNoInteractions();
        then(genreService).shouldHaveNoInteractions();
        then(characterService).shouldHaveNoInteractions();
        // then verify result is correct and has the expected data
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for delete a Movie finding it by ID")
    @Test
    public void givenMovieId_whenDeleteMovie_thenMovieDeletedFieldIsTrue() {
        // given
        final String movieId = movie.getId().toString();
        given(movieRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(movie));

        // when
        movieService.deleteMovie(movieId);

        // then verify mock interaction
        then(movieRepository).should(times(1)).findById(UUID.fromString(movieId));
        then(movieRepository).should(times(1)).delete(movie);
    }

    @DisplayName(value = "JUnit Test for delete a Movie but the movie is not present in the database")
    @Test
    public void givenMovieId_whenTryToFindTheMovieToDelete_thenThrowsEntityNotFound() {
        // given
        final String movieId = UUID.randomUUID().toString();
        final String errorMsg = STR."Movie not found for ID \{movieId}";

        // when
        Throwable result = catchThrowable(() -> movieService.deleteMovie(movieId));

        // then verify mock interaction
        then(movieRepository).should(times(1)).findById(UUID.fromString(movieId));
        then(movieRepository).shouldHaveNoMoreInteractions();
        // then verify result contains the expected data
        assertThat(result).isNotNull().isInstanceOf(EntityNotFoundException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for delete Movie providing an invalid ID format")
    @Test
    public void givenInvalidIdFormat_whenDeleteMovie_thenThrowsInvalidUUIDFormat() {
        // given
        final String invalidIdFormat = "123-456-7890";
        final String errorMsg = STR."Invalid UUID string: \{invalidIdFormat}";

        // when
        Throwable result = catchThrowable(() -> movieService.deleteMovie(invalidIdFormat));

        // then verify mock interaction
        then(movieRepository).shouldHaveNoInteractions();
        // then verify result contains the expected data
        assertThat(result).isNotNull().isInstanceOf(InvalidUUIDFormatException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for delete Movie providing a null ID")
    @Test
    public void givenNullId_whenDeleteMovie_thenThrowsInvalidParameter() {
        // given
        final String errorMsg = "The provided ID is invalid or null";

        // when
        Throwable result = catchThrowable(() -> movieService.deleteMovie(null));

        // then verify mock interaction
        then(movieRepository).shouldHaveNoInteractions();
        // then verify result contains the expected data
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for get Movie (DTO) by ID")
    @Test
    public void givenMovieId_whenGetMovieById_thenReturnTheMovieFound() {
        // given
        final String movieId = movie.getId().toString();
        given(movieRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(movie));
        given(movieMapper.toDTO(any(Movie.class))).willReturn(movieResponse);

        // when
        MovieResponseDto result = movieService.getMovieById(movieId);

        // then verify mocks interactions
        then(movieRepository).should(times(1)).findById(UUID.fromString(movieId));
        then(movieMapper).should(times(1)).toDTO(movie);
        // then verify result contains expected data
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(movieResponse);
    }

    @DisplayName(value = "JUnit Test for get Movie (DTO) by ID and the movie is not present in the database")
    @Test
    public void givenMovieId_whenGetMovieById_thenThrowsEntityNotFound() {
        // given
        final String movieId = UUID.randomUUID().toString();
        final String errorMsg = STR."Movie not found for ID \{movieId}";

        // when
        Throwable result = catchThrowable(() -> movieService.getMovieById(movieId));

        //then verify mock interactions
        then(movieRepository).should(times(1)).findById(UUID.fromString(movieId));
        then(movieMapper).shouldHaveNoInteractions();
        // then verify result contains expected data
        assertThat(result).isNotNull().isInstanceOf(EntityNotFoundException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for get Movie (DTO) by ID providing a null ID")
    @Test
    public void givenNullId_whenGetMovieById_thenThrowsInvalidParameter() {
        // given
        final String errorMsg = "The provided Movie ID is invalid";

        // when
        Throwable result = catchThrowable(() -> movieService.getMovieById(null));

        // then verify mocks interactions
        then(movieRepository).shouldHaveNoInteractions();
        then(movieMapper).shouldHaveNoInteractions();
        // then verify result contains expected data
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for get Movie (DTO) by ID providing and invalid ID format")
    @Test
    public void givenInvalidIdFormat_whenGetMovieById_thenThrowsInvalidUUIDFormat() {
        // given
        final String invalidIdFormat = "123-4556-789-1122";
        final String errorMsg = STR."Invalid UUID string: \{invalidIdFormat}";

        // when
        Throwable result = catchThrowable(() -> movieService.getMovieById(invalidIdFormat));

        // then verify mocks interactions
        then(movieRepository).shouldHaveNoInteractions();
        then(movieMapper).shouldHaveNoInteractions();
        // then verify result contains expected data
        assertThat(result).isNotNull().isInstanceOf(InvalidUUIDFormatException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for append Character to Movie")
    @Test
    public void givenMovieIdAndCharacter_whenAppendCharacterToMovie_thenReturnMovieWithCharacterAdded() {
        // given
        final UUID movieId = movie.getId();
        final Character character = mock(Character.class);
        given(movieRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(movie));
        given(movieRepository.save(any(Movie.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Movie result = movieService.appendCharacterToMovie(movieId, character);

        //then verify mock interactions
        then(movieRepository).should(times(1)).findById(movieId);
        then(movieRepository).should(times(1)).save(movie);
        // then verify result contains the expected data
        assertThat(result).isNotNull();
        assertThat(result.getCharacters()).isNotEmpty().hasSize(2);
        assertThat(result.getCharacters()).contains(character);
    }

    @DisplayName(value = "JUnit Test for append Character to Movie providing a null ID")
    @Test
    public void givenNullId_whenVerifyNullabilityOfIdBeforeAppendCharacterToMovie_thenThrowsInvalidParameter() {
        // given
        final Character character = mock(Character.class);
        final String errorMsg = "The provided Movie ID is invalid";

        // when
        Throwable result = catchThrowable(() -> movieService.appendCharacterToMovie(null, character));

        // then verify mock interaction
        then(movieRepository).shouldHaveNoInteractions();
        // then verify result contains expected data
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for append Character to Movie and the movie is not present in the database")
    @Test
    public void givenCharacterAndId_whenFindMovieToAppendCharacter_thenThrowsEntityNotFound() {
        // given
        final UUID movieId = UUID.randomUUID();
        final Character character = mock(Character.class);
        final String errorMsg = STR."Movie not found for ID \{movieId}";

        // when
        Throwable result = catchThrowable(() -> movieService.appendCharacterToMovie(movieId, character));

        // then verify mock interactions
        then(movieRepository).should(times(1)).findById(movieId);
        then(movieRepository).shouldHaveNoMoreInteractions();
        // then verify result contains expected data
        assertThat(result).isNotNull().isInstanceOf(EntityNotFoundException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for remove Character from Movie")
    @Test
    public void givenMovieIdAndCharacterToRemove_whenRemoveCharacterFromMovie_thenReturnMovieWithoutCharacter() {
        /*
         * In this test the movie already contains one character. We are going to add a new character
         * to the current movie's list and try to remove it, so the result is that
         * the list only contains one character, the original one.
         */

        // given
        final UUID movieId = movie.getId();
        final Character character = mock(Character.class);
        movie.getCharacters().add(character); // adding character before try to remove it
        given(movieRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(movie));
        given(movieRepository.save(any(Movie.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Movie result = movieService.removeCharacterFromMovie(movieId, character);

        //then verify mock interactions
        then(movieRepository).should(times(1)).findById(movieId);
        then(movieRepository).should(times(1)).save(movie);
        // then verify result contains the expected data
        assertThat(result).isNotNull();
        assertThat(result.getCharacters()).isNotEmpty().hasSize(1);
        assertThat(result.getCharacters()).doesNotContain(character);
    }

    @DisplayName(value = "JUnit Test for remove Character from Movie providing a null ID")
    @Test
    public void givenNullId_whenVerifyNullabilityOfIdBeforeRemovingCharacterFromMovie_thenThrowsInvalidParameter() {
        // given
        final Character character = mock(Character.class);
        final String errorMsg = "The provided Movie ID is invalid";

        // when
        Throwable result = catchThrowable(() -> movieService.removeCharacterFromMovie(null, character));

        // then verify mock interaction
        then(movieRepository).shouldHaveNoInteractions();
        // then verify result contains expected data
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for remove Character from Movie and the movie is not present in the database")
    @Test
    public void givenCharacterAndId_whenFindMovieToRemoveCharacter_thenThrowsEntityNotFound() {
        // given
        final UUID movieId = UUID.randomUUID();
        final Character character = mock(Character.class);
        final String errorMsg = STR."Movie not found for ID \{movieId}";

        // when
        Throwable result = catchThrowable(() -> movieService.removeCharacterFromMovie(movieId, character));

        // then verify mock interactions
        then(movieRepository).should(times(1)).findById(movieId);
        then(movieRepository).shouldHaveNoMoreInteractions();
        // then verify result contains expected data
        assertThat(result).isNotNull().isInstanceOf(EntityNotFoundException.class).hasMessage(errorMsg);
    }

    @DisplayName(value = "JUnit Test for list all Movies without specification")
    @Test
    public void givenPageNumber_whenListMovies_thenReturnAllMoviesPaginated() {
        // given
        final int pageNumber = 0;
        PageRequest pageable = PageRequest.of(pageNumber, ELEMENTS_PER_PAGE);
        given(movieRepository.findAll(movieSpec.getByFilters(anyString(), anyString(), anyString()), pageable))
                .willReturn(new PageImpl<>(List.of(movie)));
        given(movieMapper.toDTO(any(Movie.class))).willReturn(movieResponse);

        // when
        Page<MovieResponseDto> result = movieService.listMovies(pageNumber, "", "", "");

        //then verify mocks interactions
        then(movieRepository).should(times(1)).findAll(movieSpec.getByFilters("", "", ""), pageable);
        then(movieMapper).should(times(1)).toDTO(movie);
        // then assert on result verifying that contains expected data
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1L);
        //...as the result only contains one single object, we apply recursive comparison against other object
        assertThat(result.getContent().getFirst()).usingRecursiveComparison().isEqualTo(movieResponse);
    }
}
