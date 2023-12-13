package com.disney.unit.service;

import com.disney.model.dto.request.CharacterRequestDto;
import com.disney.model.dto.request.CharacterUpdateRequestDto;
import com.disney.model.dto.response.CharacterResponseDto;
import com.disney.model.dto.response.basic.MovieBasicInfoResponseDto;
import com.disney.model.entity.Character;
import com.disney.model.entity.Genre;
import com.disney.model.entity.Movie;
import com.disney.model.mapper.CharacterMapper;
import com.disney.repository.CharacterRepository;
import com.disney.repository.specification.CharacterSpecification;
import com.disney.service.MovieService;
import com.disney.service.implement.CharacterServiceImpl;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.BDDAssertions;
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
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class CharacterServiceTest {
    @Mock
    private CharacterRepository characterRepository;
    @Mock
    private CharacterMapper characterMapper;
    @Mock
    private MovieService movieService;
    @Mock
    private CharacterSpecification characterSpec;
    @InjectMocks
    private CharacterServiceImpl characterService;
    @Captor
    private ArgumentCaptor<Character> characterArgumentCaptor;

    private Character characterOne;
    private Character characterTwo;
    private Movie movie;
    private CharacterRequestDto requestDto; // only info related to character
    private CharacterRequestDto completeRequestDto; // movies to append included
    private CharacterResponseDto characterOneResponseDto;
    private CharacterResponseDto characterTwoResponseDto;
    private PageRequest pageable;

    @BeforeEach
    void setUp() {
        movie = Movie.builder()
                .id(UUID.randomUUID())
                .image("movie-image.jpg")
                .title("Movie Title")
                .creationDate(LocalDate.now())
                .rate(5)
                .genre(Genre.builder().build())
                .build();
        characterOne = Character.builder()
                .id(UUID.randomUUID())
                .image("character-image.jpg")
                .name("Character Name")
                .age(31)
                .weight(92.7)
                .history("Character history description")
                .build();
        characterTwo = Character.builder()
                .id(UUID.randomUUID())
                .image("character-two-image.jpg")
                .name("Character Two Name")
                .age(27)
                .weight(70)
                .history("Character Two history description")
                .movies(new HashSet<>(Set.of(movie)))
                .build();
        requestDto = CharacterRequestDto.builder()
                .image("character-image.jpg")
                .name("Character Name")
                .age(31)
                .weight(92.7)
                .history("Character history description")
                .moviesId(emptySet())
                .build();
        completeRequestDto = CharacterRequestDto.builder()
                .image("character-image.jpg")
                .name("Character Name")
                .age(31)
                .weight(92.7)
                .history("Character history description")
                .moviesId(Set.of(movie.getId().toString()))
                .build();
        characterOneResponseDto = CharacterResponseDto.builder()
                .id(characterOne.getId().toString())
                .image(characterOne.getImage())
                .name(characterOne.getName())
                .age(characterOne.getAge())
                .weight(characterOne.getWeight())
                .history(characterOne.getHistory())
                .movies(emptySet())
                .build();
        characterTwoResponseDto = CharacterResponseDto.builder()
                .id(characterTwo.getId().toString())
                .image(characterTwo.getImage())
                .name(characterTwo.getName())
                .age(characterTwo.getAge())
                .weight(characterTwo.getWeight())
                .history(characterTwo.getHistory())
                .movies(Set.of(mock(MovieBasicInfoResponseDto.class)))
                .build();
        pageable = PageRequest.of(0, ELEMENTS_PER_PAGE);
    }

    @DisplayName(value = "JUnit Test for successfully create and save a Character (without movies to append)")
    @Test
    public void givenCharacterRequestObject_whenCreateCharacter_thenSaveCharacterInDDBB() {
        // given
        given(characterRepository.existsByName(anyString())).willReturn(false);
        given(characterMapper.toEntity(requestDto)).willReturn(characterOne);
        given(characterRepository.save(any(Character.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        characterService.createCharacter(requestDto);

        //then
        then(characterMapper).should(times(1)).toEntity(requestDto);
        then(characterRepository).should(times(1)).save(characterArgumentCaptor.capture());
        assertThat(characterArgumentCaptor.getValue()).isNotNull().usingRecursiveComparison().isEqualTo(characterOne);
    }

    @DisplayName(value = "JUnit Test for successfully create and save a Character (with list of movies to append)")
    @Test
    public void givenCharacterRequestObjectWithMoviesToLink_whenCreateCharacter_thenSaveCharacterInDDBB() {
        // given
        given(characterRepository.existsByName(anyString())).willReturn(false);
        given(characterMapper.toEntity(completeRequestDto)).willReturn(characterOne);
        given(movieService.appendCharacterToMovie(any(UUID.class), any(Character.class)))
                .willAnswer(invocation -> {
                    Character argumentCharacter = invocation.getArgument(1);
                    movie.getCharacters().add(argumentCharacter);
                    return movie;
                });
        given(characterRepository.save(any(Character.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        characterService.createCharacter(completeRequestDto);

        //then
        then(characterMapper).should(times(1)).toEntity(completeRequestDto);
        then(movieService).should(times(1)).appendCharacterToMovie(movie.getId(), characterOne);
        then(characterRepository).should(times(1)).save(characterArgumentCaptor.capture());
        assertThat(characterArgumentCaptor.getValue())
                .isNotNull().usingRecursiveComparison().ignoringFields("movies").isEqualTo(characterOne);
        assertThat(characterArgumentCaptor.getValue().getMovies()).isNotEmpty();
        assertThat(characterArgumentCaptor.getValue().getMovies()).containsExactly(movie);
    }

    @DisplayName(value = "JUnit Test for create Character with name taken and throws EntityExistsException")
    @Test
    public void givenTakenName_whenCreateCharacter_thenThrowsEntityExistsException() {
        // given
        final String expectedMessage = "The character 'Character Name' is already registered";
        given(characterRepository.existsByName(anyString())).willReturn(true);

        // when
        Throwable result = catchThrowable(() -> characterService.createCharacter(requestDto));

        //then
        assertThat(result).isNotNull().isInstanceOf(EntityExistsException.class).hasMessage(expectedMessage);
        then(characterRepository).shouldHaveNoMoreInteractions();
        then(movieService).shouldHaveNoInteractions();
        then(characterMapper).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for create Character with a null CharacterRequestDto object and throws")
    @Test
    public void givenNullRequestObject_whenTryToCreateCharacter_thenThrowsInvalidParameterException() {
        // given
        final String expectedMessage = "Invalid argument passed: Character object";

        // when
        Throwable result = catchThrowable(() -> characterService.createCharacter(null));

        //then
        BDDAssertions.then(result).isInstanceOf(InvalidParameterException.class).hasMessage(expectedMessage);
    }

    @DisplayName(value = "JUnit Test for create Character with empty values in CharacterRequestDto object and throws")
    @Test
    public void givenRequestObject_whenTryToCreateCharacter_thenThrowsInvalidParameterExceptionForEmptyName() {
        // given
        final CharacterRequestDto nullRequest = CharacterRequestDto.builder().build();
        final String expectedMessage = "Invalid argument passed: Character object";

        // when
        Throwable result = catchThrowable(() -> characterService.createCharacter(nullRequest));

        //then
        BDDAssertions.then(result).isInstanceOf(InvalidParameterException.class).hasMessage(expectedMessage);
        then(movieService).shouldHaveNoInteractions();
        then(characterRepository).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for update Character information only (not movies)")
    @Test
    public void givenUpdateRequest_whenUpdateCharacter_thenReturnTheCharacterUpdated() {
        // given
        final String characterId = characterOne.getId().toString();
        final CharacterUpdateRequestDto updateRequest = CharacterUpdateRequestDto.builder()
                .image("new-image.jpg")
                .name("New Character Name")
                .age(210)
                .weight(120.5)
                .history("New character history.")
                .moviesToUnlink(emptySet())
                .moviesWhereAppears(emptySet())
                .build();
        final CharacterResponseDto expectedResponse = CharacterResponseDto.builder()
                .id(characterId)
                .image(updateRequest.image())
                .name(updateRequest.name())
                .age(updateRequest.age())
                .weight(updateRequest.weight())
                .history(updateRequest.history())
                .movies(emptySet())
                .build();

        given(characterRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(characterOne));
        given(characterRepository.save(any(Character.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(characterMapper.toDTO(any(Character.class))).willReturn(expectedResponse);

        // when
        CharacterResponseDto result = characterService.updateCharacter(characterId, updateRequest);


        //then
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(expectedResponse);
        then(characterRepository).should(times(1)).findById(characterOne.getId());
        then(characterRepository).should(times(1)).save(any(Character.class));
        then(characterMapper).should(times(1)).toDTO(characterOne);
        then(movieService).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for update Character removing a movie without changing any other value")
    @Test
    public void givenUpdateRequestWithAMovieToRemove_whenUpdateCharacter_thenReturnCharacterUpdatedWithoutTheMovie() {
        // ... adding movie to character and vice versa before try to remove each one
        characterOne.getMovies().add(movie);
        movie.getCharacters().add(characterOne);

        // given
        final String characterId = characterOne.getId().toString();
        final CharacterUpdateRequestDto updateRequest = CharacterUpdateRequestDto.builder()
                .moviesToUnlink(Set.of(movie.getId().toString()))
                .build();

        given(characterRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(characterOne));
        given(movieService.removeCharacterFromMovie(any(UUID.class), any(Character.class))).willReturn(movie);
        given(characterRepository.save(any(Character.class))).willReturn(characterOne);
        given(characterMapper.toDTO(any(Character.class))).willReturn(characterOneResponseDto);

        // when
        CharacterResponseDto result = characterService.updateCharacter(characterId, updateRequest);

        //then
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(characterOneResponseDto);
        then(characterRepository).should(times(1)).findById(characterOne.getId());
        then(movieService).should(times(1)).removeCharacterFromMovie(movie.getId(), characterOne);
        then(characterRepository).should(times(1)).save(characterOne);
        then(characterMapper).should(times(1)).toDTO(characterOne);
    }

    @DisplayName(value = "JUnit Test for update Character that has no movies by adding one (original values intact)")
    @Test
    public void givenUpdateRequestWithAMovieToAdd_whenUpdateCharacter_thenReturnCharacterUpdatedWithTheMovie() {
        // ... remove all movies associated to this character
        characterTwo.getMovies().clear();

        // given
        final String characterId = characterTwo.getId().toString();
        final CharacterUpdateRequestDto updateRequest = CharacterUpdateRequestDto.builder()
                .moviesWhereAppears(Set.of(movie.getId().toString()))
                .build();

        given(characterRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(characterTwo));
        given(movieService.appendCharacterToMovie(any(UUID.class), any(Character.class))).willAnswer(
                invocation -> {
                    Character argumentCharacter = invocation.getArgument(1);
                    movie.getCharacters().add(argumentCharacter);
                    return movie;
                }
        );
        given(characterRepository.save(any(Character.class))).willAnswer(
                invocation -> {
                    Character character = invocation.getArgument(0);
                    character.getMovies().add(any(Movie.class));
                    return character;
                });
        given(characterMapper.toDTO(any(Character.class))).willReturn(characterTwoResponseDto);

        // when
        CharacterResponseDto result = characterService.updateCharacter(characterId, updateRequest);

        //then
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(characterTwoResponseDto);
        then(characterRepository).should(times(1)).findById(characterTwo.getId());
        then(movieService).should(times(1)).appendCharacterToMovie(movie.getId(), characterTwo);
        then(characterRepository).should(times(1)).save(characterTwo);
        then(characterMapper).should(times(1)).toDTO(characterTwo);
    }

    @DisplayName(value = "JUnit Test for update Character with invalid ID and throws InvalidParameterException")
    @Test
    public void givenInvalidId_whenUpdateCharacter_thenThrowsInvalidParameterException() {
        // given
        String expectedMessage = "Invalid parameter provided: Character ID";

        // when
        Throwable result = catchThrowable(() -> characterService.updateCharacter(null, null));

        //then
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(expectedMessage);
        then(characterRepository).shouldHaveNoInteractions();
        then(movieService).shouldHaveNoInteractions();
        then(characterMapper).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for get Character (DTO) by ID")
    @Test
    public void givenId_whenGetCharacterById_thenReturnCharacterResponse() {
        // given
        final String characterId = characterOne.getId().toString();
        given(characterRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(characterOne));
        given(characterMapper.toDTO(any(Character.class))).willReturn(characterOneResponseDto);

        // when
        CharacterResponseDto result = characterService.getCharacterById(characterId);

        //then
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(characterOneResponseDto);
        then(characterRepository).should(times(1)).findById(characterOne.getId());
        then(characterMapper).should(times(1)).toDTO(characterOne);
    }

    @DisplayName(value = "JUnit Test for EntityNotFoundException when get Character (DTO) by ID")
    @Test
    public void givenId_whenGetCharacterById_thenThrowsEntityNotFoundException() {
        // given
        final String characterId = UUID.randomUUID().toString();
        final String expectedMessage = STR."Character not found for ID \{characterId}";
        given(characterRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // when
        Throwable result = catchThrowable(() -> characterService.getCharacterById(characterId));

        //then
        assertThat(result).isNotNull().isInstanceOf(EntityNotFoundException.class).hasMessage(expectedMessage);
        then(characterMapper).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for InvalidParameterException when get Character (DTO) with null ID")
    @Test
    public void givenNullId_whenGetCharacterById_thenThrowsInvalidParameterException() {
        // given
        String expectedMessage = "Invalid parameter value: characterId";

        // when
        Throwable result = catchThrowable(() -> characterService.getCharacterById((String) null));

        //then
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(expectedMessage);
        then(characterRepository).shouldHaveNoInteractions();
        then(characterMapper).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for get Character (Entity) by ID")
    @Test
    public void givenId_whenGetCharacterById_thenReturnCharacter() {
        // given
        final UUID characterId = characterOne.getId();
        given(characterRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(characterOne));

        // when
        Character result = characterService.getCharacterById(characterId);

        //then
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(characterOne);
        then(characterRepository).should(times(1)).findById(characterOne.getId());
    }

    @DisplayName(value = "JUnit Test for EntityNotFoundException when get Character (Entity) by ID")
    @Test
    public void givenUUID_whenGetCharacterById_thenThrowsEntityNotFoundException() {
        // given
        final UUID characterId = UUID.randomUUID();
        final String expectedMessage = STR."Character not found for ID \{characterId}";
        given(characterRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // when
        Throwable result = catchThrowable(() -> characterService.getCharacterById(characterId));

        //then
        assertThat(result).isNotNull().isInstanceOf(EntityNotFoundException.class).hasMessage(expectedMessage);
    }

    @DisplayName(value = "JUnit Test for InvalidParameterException when get Character (Entity) with null ID")
    @Test
    public void givenNullUUID_whenGetCharacterById_thenThrowsInvalidParameterException() {
        // given
        String expectedMessage = "Invalid parameter value: characterId";

        // when
        Throwable result = catchThrowable(() -> characterService.getCharacterById((UUID) null));

        //then
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(expectedMessage);
        then(characterRepository).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for successfully delete Character")
    @Test
    public void givenId_whenDeleteCharacter_thenCharacterIsNotPresentInDDBB() {
        // given
        final String characterId = characterOne.getId().toString();
        given(characterRepository.findById(any(UUID.class))).willReturn(Optional.ofNullable(characterOne));
        willDoNothing().given(characterRepository).delete(characterOne);

        // when
        characterService.deleteCharacter(characterId);

        //then
        then(characterRepository).should(times(1)).delete(characterOne);
    }

    @DisplayName(value = "JUnit Test for delete Character with null ID and throws InvalidParameterException")
    @Test
    public void givenInvalidId_whenDeleteCharacter_thenThrowsInvalidParameterException() {
        // given
        final String expectedMessage = "The provided ID is invalid or null";

        // when
        Throwable result = catchThrowable(() -> characterService.deleteCharacter(null));

        //then
        assertThat(result).isNotNull().isInstanceOf(InvalidParameterException.class).hasMessage(expectedMessage);
        then(characterRepository).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for delete Character and throws EntityNotFoundException")
    @Test
    public void givenId_whenSearchFoDeleteCharacter_thenThrowsEntityNotFoundException() {
        // given
        final String characterId = UUID.randomUUID().toString();
        final String expectedMessage = STR."Character not found for ID \{characterId}";
        given(characterRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // when
        Throwable result = catchThrowable(() -> characterService.deleteCharacter(characterId));

        //then
        assertThat(result).isNotNull().isInstanceOf(EntityNotFoundException.class).hasMessage(expectedMessage);
        then(characterRepository).shouldHaveNoMoreInteractions();
    }

    @DisplayName(value = "JUnit Test for list all Characters without specifying any filter (name, age or movies)")
    @Test
    public void givenPageNumber_whenListAllCharacters_thenReturnAllCharactersFromDDBB() {
        // given
        final int pageNumber = 0, zeroAge = 0;
        final String emptyName = "";
        final Set<String> emptyMovies = emptySet();
        given(characterRepository.findAll(characterSpec.getByFilters(emptyName, zeroAge, emptyMovies), pageable))
                .willReturn(new PageImpl<>(List.of(characterOne, characterTwo)));
        given(characterMapper.toDTO(characterOne)).willReturn(characterOneResponseDto);
        given(characterMapper.toDTO(characterTwo)).willReturn(characterTwoResponseDto);

        // when
        Page<CharacterResponseDto> result = characterService.listCharacters(pageNumber, emptyName, zeroAge, emptyMovies);

        //then
        assertThat(result).isNotNull();
        assertThat(result.hasContent()).isTrue();
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getContent()).isNotEmpty().contains(characterOneResponseDto, characterTwoResponseDto);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @DisplayName(value = "JUnit Test for list all Characters filtering by name")
    @Test
    public void givenPageNumberAndName_whenListCharacters_thenReturnListFilteredByName() {
        // given
        final int pageNumber = 0;
        final String name = characterOne.getName();
        given(characterRepository.findAll(characterSpec.getByFilters(name, 0, emptySet()), pageable))
                .willReturn(new PageImpl<>(List.of(characterOne)));
        given(characterMapper.toDTO(characterOne)).willReturn(characterOneResponseDto);

        // when
        Page<CharacterResponseDto> result = characterService.listCharacters(pageNumber, name, 0, emptySet());

        //then
        assertThat(result).isNotNull();
        assertThat(result.hasContent()).isTrue();
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent()).isNotEmpty().contains(characterOneResponseDto);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }
}
