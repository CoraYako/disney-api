package com.disney.service.implement;

import com.disney.model.InvalidUUIDFormatException;
import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.request.GenreUpdateRequestDto;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.model.entity.Genre;
import com.disney.model.mapper.GenreMapper;
import com.disney.repository.GenreRepository;
import com.disney.service.GenreService;
import com.disney.util.ApiUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.security.InvalidParameterException;
import java.util.Objects;
import java.util.UUID;

@Service
@Validated
public class GenreServiceImpl implements GenreService {
    private final Logger logger = LoggerFactory.getLogger(GenreServiceImpl.class);
    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    public GenreServiceImpl(GenreRepository genreRepository, GenreMapper genreMapper) {
        this.genreRepository = genreRepository;
        this.genreMapper = genreMapper;
    }

    @Override
    @Transactional(rollbackFor = {InvalidParameterException.class, EntityExistsException.class})
    public void createGenre(GenreRequestDto requestDto) {
        if (Objects.isNull(requestDto) || !StringUtils.hasLength(requestDto.name()))
            throw new InvalidParameterException("Null argument passed: genre object");
        if (genreRepository.existsByName(requestDto.name()))
            throw new EntityExistsException("The Genre '%s' is already registered.".formatted(requestDto.name()));
        Genre genre = genreMapper.toEntity(requestDto);
        genre = genreRepository.save(genre);
        logger.info("Genre created with name {} and ID {}", genre.getName(), genre.getId());
    }

    @Override
    @Transactional(rollbackFor = {
            IllegalArgumentException.class,
            EntityNotFoundException.class,
            InvalidParameterException.class,
            InvalidUUIDFormatException.class
    })
    public GenreResponseDto updateGenre(String id, GenreUpdateRequestDto requestDto) {
        if (Objects.isNull(id))
            throw new InvalidParameterException("Invalid argument passed: genre ID");
        Genre genreToUpdate = genreRepository.findById(ApiUtils.getUUIDFromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Genre not found for ID %s".formatted(id)));
        genreToUpdate.setName(requestDto.name());
        return genreMapper.toDTO(genreRepository.save(genreToUpdate));
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponseDto getGenreById(String id) {
        if (Objects.isNull(id))
            throw new InvalidParameterException("Invalid argument ID supplied");
        return genreRepository.findById(ApiUtils.getUUIDFromString(id)).map(genreMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Genre not found for ID %s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Genre getGenreById(UUID id) {
        if (Objects.isNull(id))
            throw new InvalidParameterException("Invalid argument ID supplied");
        return genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre not found for ID %s".formatted(id)));
    }

    @Override
    public Page<GenreResponseDto> listMovieGenres(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, ApiUtils.ELEMENTS_PER_PAGE);
        return genreRepository.findAll(pageable).map(genreMapper::toDTO);
    }
}
