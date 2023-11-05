package com.disney.service.implement;

import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.request.GenreUpdateRequestDto;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.model.entity.Genre;
import com.disney.model.mapper.GenreMapper;
import com.disney.repository.GenreRepository;
import com.disney.service.GenreService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.UUID;

@Service
@Validated
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    public GenreServiceImpl(GenreRepository genreRepository, GenreMapper genreMapper) {
        this.genreRepository = genreRepository;
        this.genreMapper = genreMapper;
    }

    @Override
    @Transactional(rollbackFor = {IllegalArgumentException.class, EntityExistsException.class})
    public void createGenre(GenreRequestDto requestDto) {
        if (Objects.isNull(requestDto) || StringUtils.hasLength(requestDto.name()))
            throw new IllegalArgumentException("Null argument passed: genre object");
        if (genreRepository.existsByName(requestDto.name()))
            throw new EntityExistsException("This Genre is already registered.");
        Genre genre = genreMapper.toEntity(requestDto);
        genreRepository.save(genre);
    }

    @Override
    @Transactional(rollbackFor = {IllegalArgumentException.class, EntityNotFoundException.class})
    public GenreResponseDto updateGenre(UUID id, GenreUpdateRequestDto requestDto) {
        if (Objects.isNull(id) || Objects.isNull(requestDto))
            throw new IllegalArgumentException("Null argument passed: genre object to update");
        Genre genreToUpdate = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre not found for ID %s".formatted(id)));
        genreToUpdate.setName(requestDto.name());
        return genreMapper.toDTO(genreRepository.save(genreToUpdate));
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponseDto getGenreDtoById(UUID id) {
        if (Objects.isNull(id))
            throw new IllegalArgumentException("Invalid argument ID supplied");
        return genreRepository.findById(id).map(genreMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Genre not found for ID %s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Genre getGenreById(UUID id) {
        if (Objects.isNull(id))
            throw new IllegalArgumentException("Invalid argument ID supplied");
        return genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre not found for ID %s".formatted(id)));
    }
}
