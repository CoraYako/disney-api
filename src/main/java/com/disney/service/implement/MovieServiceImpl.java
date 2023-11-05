package com.disney.service.implement;

import com.disney.model.dto.request.MovieRequestDto;
import com.disney.model.dto.request.MovieUpdateRequestDto;
import com.disney.model.dto.response.MovieResponseDto;
import com.disney.model.entity.Movie;
import com.disney.model.mapper.MovieMapper;
import com.disney.repository.MovieRepository;
import com.disney.repository.specification.MovieSpecification;
import com.disney.service.CharacterService;
import com.disney.service.GenreService;
import com.disney.service.MovieService;
import com.disney.util.ApiUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final MovieSpecification movieSpec;
    private final CharacterService characterService;
    private final GenreService genreService;

    public MovieServiceImpl(MovieRepository movieRepository, MovieMapper movieMapper, MovieSpecification movieSpec,
                            CharacterService characterService, GenreService genreService) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.movieSpec = movieSpec;
        this.characterService = characterService;
        this.genreService = genreService;
    }

    @Override
    @Transactional(rollbackFor = {IllegalArgumentException.class, EntityExistsException.class})
    public void createMovie(MovieRequestDto requestDto) {
        if (Objects.isNull(requestDto) || !StringUtils.hasLength(requestDto.title()))
            throw new IllegalArgumentException("Invalid parameter value: movie");
        if (movieRepository.existsByTitle(requestDto.title()))
            throw new EntityExistsException("The movie %s already exist".formatted(requestDto.title()));
        Movie movie = movieMapper.toEntity(requestDto);
        movie.getCharacters().addAll(
                requestDto.charactersId().stream()
                        .map(characterId -> characterService.getCharacterById(UUID.fromString(characterId)))
                        .collect(Collectors.toUnmodifiableSet())
        );
        movieRepository.save(movie);
    }

    @Override
    @Transactional(rollbackFor = {
            IllegalArgumentException.class,
            EntityExistsException.class,
            EntityNotFoundException.class
    })
    public MovieResponseDto updateMovie(String id, MovieUpdateRequestDto requestDto) {
        if (Objects.isNull(id) || Objects.isNull(requestDto) || !StringUtils.hasLength(requestDto.title()))
            throw new IllegalArgumentException("Invalid argument passed: movie");
        if (movieRepository.existsByTitle(requestDto.title()))
            throw new EntityExistsException("The movie %s already exist".formatted(requestDto.title()));
        Movie movieToUpdate = movieRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Movie not found for ID %s".formatted(id)));
        movieToUpdate.setImage(requestDto.image());
        movieToUpdate.setTitle(requestDto.title());
        if (Objects.nonNull(requestDto.creationDate()) && !requestDto.creationDate().trim().isEmpty())
            movieToUpdate.setCreationDate(LocalDate.parse(requestDto.creationDate(), ApiUtils.OF_PATTERN));
        if (Objects.nonNull(requestDto.genreId()) && !requestDto.genreId().trim().isEmpty())
            movieToUpdate.setGenre(genreService.getGenreById(UUID.fromString(requestDto.genreId())));
        // verifies if the current list is not empty and performs an operation to add new characters
        if (!CollectionUtils.isEmpty(requestDto.charactersToAdd()))
            movieToUpdate.getCharacters().addAll(
                    requestDto.charactersToAdd().stream()
                            .map(characterId -> characterService.getCharacterById(UUID.fromString(characterId)))
                            .collect(Collectors.toUnmodifiableSet())
            );
        // verifies if the current list is not empty and performs an operation to remove the characters
        if (!CollectionUtils.isEmpty(requestDto.charactersToRemove()))
            movieToUpdate.getCharacters().removeAll(
                    requestDto.charactersToRemove().stream()
                            .map(characterId -> characterService.getCharacterById(UUID.fromString(characterId)))
                            .collect(Collectors.toUnmodifiableSet())
            );
        return movieMapper.toDTO(movieRepository.save(movieToUpdate));
    }

    @Override
    @Transactional(rollbackFor = {IllegalArgumentException.class, EntityNotFoundException.class})
    public void deleteMovie(String id) {
        if (Objects.isNull(id))
            throw new IllegalArgumentException("The provided ID is invalid or null");
        Movie movieFound = movieRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Movie not found for ID %s".formatted(id)));
        movieRepository.delete(movieFound);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponseDto getMovieById(String id) {
        if (Objects.isNull(id))
            throw new IllegalArgumentException("The provided Movie ID is invalid");
        Movie movieFound = movieRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Movie not found for ID %s".formatted(id)));
        return movieMapper.toDTO(movieFound);
    }

    @Override
    public Movie getMovieById(UUID id) {
        if (Objects.isNull(id))
            throw new IllegalArgumentException("The provided Movie ID is invalid");
        return movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found for ID %s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponseDto> listMovies(int pageNumber, String title, String genre, String order) {
        Pageable pageable = PageRequest.of(pageNumber, 10);
        pageable.next().getPageNumber();
        return movieRepository.findAll(movieSpec.getByFilters(title, genre, order), pageable)
                .map(movieMapper::toDTO);
    }
}
