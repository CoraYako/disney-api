package com.disney.service;

import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.request.GenreUpdateRequestDto;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.model.entity.Genre;
import com.disney.model.mapper.GenreMapper;
import com.disney.repository.GenreRepository;
import com.disney.service.implement.GenreServiceImpl;
import com.disney.util.ApiUtils;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class GenreServiceTest {
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private GenreMapper genreMapper;
    @InjectMocks
    private GenreServiceImpl genreService;
    @Captor
    private ArgumentCaptor<Genre> genreArgumentCaptor;
    private Genre genreOne;
    private Genre genreTwo;
    private GenreRequestDto genreRequestDto;
    private GenreResponseDto genreResponseOne;
    private GenreResponseDto genreResponseTwo;
    private GenreUpdateRequestDto updateRequestDto;

    @BeforeEach
    void setUp() {
        genreRequestDto = new GenreRequestDto("thriller");

        genreOne = Genre.builder()
                .id(UUID.randomUUID())
                .name(genreRequestDto.name())
                .movies(Collections.emptySet())
                .build();
        genreTwo = Genre.builder()
                .id(UUID.randomUUID())
                .name("animation")
                .movies(Collections.emptySet())
                .build();

        genreResponseOne = new GenreResponseDto(genreOne.getId().toString(), genreOne.getName(), Collections.emptySet());
        genreResponseTwo = new GenreResponseDto(genreTwo.getId().toString(), genreTwo.getName(), Collections.emptySet());

        updateRequestDto = new GenreUpdateRequestDto("NEW NAME VALUE");
    }

    @DisplayName(value = "JUnit Test for successfully create and save a Genre")
    @Test
    public void givenGenreObject_whenCreateGenre_thenSaveTheGenreInDDBB() {
        // given
        given(genreRepository.existsByName(genreRequestDto.name())).willReturn(false);
        given(genreMapper.toEntity(genreRequestDto)).willReturn(genreOne);
        given(genreRepository.save(genreOne)).willReturn(genreOne);

        // when
        genreService.createGenre(genreRequestDto);

        //then
        then(genreMapper).should().toEntity(genreRequestDto);
        then(genreRepository).should().save(genreArgumentCaptor.capture());

        Genre genreCaptured = genreArgumentCaptor.getValue();
        assertThat(genreCaptured).usingRecursiveComparison().isEqualTo(genreOne);
    }

    @DisplayName(value = "JUnit Test for create Movie Genre and throws EntityExistsException")
    @Test
    public void givenGenreObject_whenTryToCreateGenre_thenThrowsForNameTaken() {
        // given
        String exceptionMessage = "The Genre '%s' is already registered.".formatted(genreRequestDto.name());
        given(genreRepository.existsByName(genreRequestDto.name())).willReturn(true);

        // when
        assertThatThrownBy(() -> genreService.createGenre(genreRequestDto))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage(exceptionMessage);

        //then
        then(genreRepository).should(never()).save(any(Genre.class));
        then(genreMapper).should(never()).toEntity(any(GenreRequestDto.class));
    }

    @DisplayName(value = "JUnit Test for create Movie Genre when GenreRequestDTO is null")
    @Test
    public void givenGenreRequest_whenTryToCreateGenre_thenThrowsForNullObject() {
        // given
        String exceptionMessage = "Null argument passed: genre object";

        // when
        assertThatThrownBy(() -> genreService.createGenre(null))
                .isInstanceOf(InvalidParameterException.class)
                .hasMessage(exceptionMessage);

        //then
        then(genreRepository).shouldHaveNoInteractions();
        then(genreMapper).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for create Movie Genre when name is null or empty")
    @Test
    public void givenGenreRequest_whenTryToCreateGenre_thenThrowsForInvalidGenreName() {
        // given
        String exceptionMessage = "Null argument passed: genre object";
        GenreRequestDto invalidGenreRequest = new GenreRequestDto("");

        // when
        assertThatThrownBy(() -> genreService.createGenre(invalidGenreRequest))
                .isInstanceOf(InvalidParameterException.class)
                .hasMessage(exceptionMessage);

        //then
        then(genreRepository).shouldHaveNoInteractions();
        then(genreMapper).should(never()).toEntity(any(GenreRequestDto.class));
    }

    @DisplayName(value = "JUnit Test for list all Genres")
    @Test
    public void givenGenreList_whenFindAll_thenReturnAPaginatedList() {
        int pageNumber = 0;
        Page<Genre> genreMockPage = new PageImpl<>(Arrays.asList(genreOne, genreTwo));
        PageRequest pageable = PageRequest.of(pageNumber, ApiUtils.ELEMENTS_PER_PAGE);

        // given
        given(genreRepository.findAll(pageable)).willReturn(genreMockPage);
        given(genreMapper.toDTO(genreOne)).willReturn(genreResponseOne);
        given(genreMapper.toDTO(genreTwo)).willReturn(genreResponseTwo);

        // when
        Page<GenreResponseDto> result = genreService.listMovieGenres(pageNumber);

        //then
        then(genreMapper).should(atLeastOnce()).toDTO(any(Genre.class));
        then(genreRepository).should().findAll(pageable);

        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result).hasSize(2).allMatch(Objects::nonNull);
        assertThat(result).containsExactly(genreResponseOne, genreResponseTwo);
    }

    @DisplayName(value = "JUnit Test for get empty Genre list")
    @Test
    public void givenNoGenreObjects_whenFindAll_thenReturnAnEmptyList() {
        int pageNumber = 0;
        Page<Genre> genreMockPage = new PageImpl<>(Collections.emptyList());
        PageRequest pageable = PageRequest.of(pageNumber, ApiUtils.ELEMENTS_PER_PAGE);

        // given
        given(genreRepository.findAll(pageable)).willReturn(genreMockPage);

        // when
        Page<GenreResponseDto> result = genreService.listMovieGenres(pageNumber);

        //then
        then(genreMapper).should(never()).toDTO(any(Genre.class));
        then(genreRepository).should(times(1)).findAll(pageable);

        assertThat(result).isNotNull().isEmpty();
        assertThat(result).hasSize(0).allMatch(Objects::isNull);
    }

    @DisplayName(value = "JUnit Test for get Genre by string ID")
    @Test
    public void givenId_whenGetGenreById_thenReturnTheConcreteGenreObject() {
        // given
        UUID uuid = genreOne.getId();
        String stringUUID = uuid.toString();
        given(genreRepository.findById(uuid)).willReturn(Optional.of(genreOne));
        given(genreMapper.toDTO(genreOne)).willReturn(genreResponseOne);

        // when
        GenreResponseDto result = genreService.getGenreById(stringUUID);

        //then
        then(genreRepository).should(times(ApiUtils.INVOKED_ONE_TIME)).findById(uuid);
        then(genreMapper).should().toDTO(genreOne);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveAssertion().isEqualTo(genreResponseOne);
    }

    @DisplayName(value = "JUnit Test for get Genre by string ID and throws EntityNotFoundException")
    @Test
    public void givenId_whenGetGenreById_thenThrowsEntityNotFoundException() {
        // given
        UUID uuid = UUID.randomUUID();
        String stringUUID = uuid.toString();
        String expectedExceptionMessage = "Genre not found for ID %s".formatted(stringUUID);

        given(genreRepository.findById(uuid)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> genreService.getGenreById(stringUUID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(expectedExceptionMessage);

        //then
        then(genreRepository).should(times(ApiUtils.INVOKED_ONE_TIME)).findById(uuid);
        then(genreMapper).should(never()).toDTO(genreOne);
    }

    @DisplayName(value = "JUnit Test for get Genre by string ID with an invalid ID")
    @Test
    public void givenInvalidId_whenGetGenreById_thenThrowsInvalidParameterException() {
        // given
        String expectedExceptionMessage = "Invalid argument ID supplied";

        // when
        assertThatThrownBy(() -> genreService.getGenreById((String) null))
                .isInstanceOf(InvalidParameterException.class)
                .hasMessage(expectedExceptionMessage);

        //then
        then(genreRepository).shouldHaveNoInteractions();
        then(genreMapper).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for get Genre by UUID")
    @Test
    public void givenUUID_whenGetGenreById_thenReturnTheConcreteGenreObject() {
        // given
        UUID uuid = genreOne.getId();
        given(genreRepository.findById(uuid)).willReturn(Optional.of(genreOne));

        // when
        Genre result = genreService.getGenreById(uuid);

        //then
        then(genreRepository).should(times(ApiUtils.INVOKED_ONE_TIME)).findById(uuid);
        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveAssertion().isEqualTo(genreOne);
    }

    @DisplayName(value = "JUnit Test for get Genre by UUID and throws EntityNotFoundException")
    @Test
    public void givenUUID_whenGetGenreById_thenThrowsEntityNotFoundException() {
        // given
        UUID uuid = UUID.randomUUID();
        String stringUUID = uuid.toString();
        String expectedExceptionMessage = "Genre not found for ID %s".formatted(stringUUID);

        given(genreRepository.findById(uuid)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> genreService.getGenreById(stringUUID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(expectedExceptionMessage);

        //then
        then(genreRepository).should(times(ApiUtils.INVOKED_ONE_TIME)).findById(uuid);
        then(genreMapper).should(never()).toDTO(genreOne);
    }

    @DisplayName(value = "JUnit Test for get Genre by UUID with an invalid UUID")
    @Test
    public void givenInvalidUUID_whenGetGenreById_thenThrowsInvalidParameterException() {
        // given
        String expectedExceptionMessage = "Invalid argument ID supplied";

        // when
        assertThatThrownBy(() -> genreService.getGenreById((UUID) null))
                .isInstanceOf(InvalidParameterException.class)
                .hasMessage(expectedExceptionMessage);

        //then
        then(genreRepository).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for successfully updates a Genre")
    @Test
    public void givenIdAndRequest_whenUpdatesAGenre_thenReturnTheGenreUpdated() {
        // given
        final String genreId = genreOne.getId().toString();
        Genre updatedGenreOne = genreOne;
        updatedGenreOne.setName(updateRequestDto.name());
        GenreResponseDto expectedResponse = new GenreResponseDto(updatedGenreOne.getId().toString(),
                updatedGenreOne.getName(), Collections.emptySet());

        given(genreRepository.findById(genreOne.getId())).willReturn(Optional.of(genreOne));
        given(genreRepository.save(updatedGenreOne)).willReturn(updatedGenreOne);
        given(genreMapper.toDTO(updatedGenreOne)).willReturn(expectedResponse);

        // when
        GenreResponseDto result = genreService.updateGenre(genreId, updateRequestDto);

        //then
        then(genreRepository).should(times(ApiUtils.INVOKED_ONE_TIME)).findById(genreOne.getId());
        then(genreRepository).should(times(ApiUtils.INVOKED_ONE_TIME)).save(updatedGenreOne);
        then(genreMapper).should(times(ApiUtils.INVOKED_ONE_TIME)).toDTO(updatedGenreOne);

        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(expectedResponse);
    }

    @DisplayName(value = "JUnit Test for update Genre and the Genre is not present in the database")
    @Test
    public void givenIdAndRequest_whenFindGenreToUpdate_thenThrowsEntityNotFoundException() {
        // given
        UUID genreUUID = UUID.randomUUID();
        String genreId = genreUUID.toString();
        String expectedMessage = "Genre not found for ID %s".formatted(genreId);

        given(genreRepository.findById(genreUUID)).willReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> genreService.updateGenre(genreId, updateRequestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(expectedMessage);

        //then
        then(genreRepository).should(times(ApiUtils.INVOKED_ONE_TIME)).findById(any(UUID.class));
        then(genreRepository).should(never()).save(any(Genre.class));
        then(genreMapper).shouldHaveNoInteractions();
    }

    @DisplayName(value = "JUnit Test for update Genre and throws for null required ID")
    @Test
    public void givenInvalidId_whenUpdateGenre_thenThrowsInvalidParameterException() {
        // given
        String expectedMessage = "Invalid argument passed: genre ID";

        // when
        assertThatThrownBy(() -> genreService.updateGenre(null, updateRequestDto))
                .isInstanceOf(InvalidParameterException.class)
                .hasMessage(expectedMessage);

        //then
        then(genreRepository).shouldHaveNoInteractions();
        then(genreRepository).shouldHaveNoInteractions();
    }
}
