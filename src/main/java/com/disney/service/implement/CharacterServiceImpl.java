package com.disney.service.implement;

import com.disney.model.InvalidUUIDFormatException;
import com.disney.model.dto.request.CharacterRequestDto;
import com.disney.model.dto.request.CharacterUpdateRequestDto;
import com.disney.model.dto.response.CharacterResponseDto;
import com.disney.model.entity.Character;
import com.disney.model.mapper.CharacterMapper;
import com.disney.repository.CharacterRepository;
import com.disney.repository.specification.CharacterSpecification;
import com.disney.service.CharacterService;
import com.disney.service.MovieService;
import com.disney.util.ApiUtils;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.security.InvalidParameterException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class CharacterServiceImpl implements CharacterService {
    private final Logger logger = LoggerFactory.getLogger(CharacterServiceImpl.class);
    private final CharacterMapper characterMapper;
    private final CharacterRepository characterRepository;
    private final CharacterSpecification characterSpec;
    private final MovieService movieService;

    public CharacterServiceImpl(CharacterMapper characterMapper, CharacterRepository characterRepository,
                                CharacterSpecification characterSpec, @Lazy MovieService movieService) {
        this.characterMapper = characterMapper;
        this.characterRepository = characterRepository;
        this.characterSpec = characterSpec;
        this.movieService = movieService;
    }

    @Override
    @Transactional(rollbackFor = {
            InvalidParameterException.class,
            EntityExistsException.class,
            InvalidUUIDFormatException.class
    })
    public void createCharacter(CharacterRequestDto requestDto) {
        if (Objects.isNull(requestDto) || !StringUtils.hasLength(requestDto.name()))
            throw new InvalidParameterException("Invalid argument passed: Character object");
        if (characterRepository.existsByName(requestDto.name()))
            throw new EntityExistsException("The character '%s' is already registered".formatted(requestDto.name()));
        // saves the current character before append the movies (if present) to the character
        Character character = characterMapper.toEntity(requestDto);

        // takes the list of movies and adds the movies to the current character and vise versa
        if (!CollectionUtils.isEmpty(requestDto.moviesId()))
            character.getMovies().addAll(requestDto.moviesId().stream()
                    .map(movieId -> movieService.appendCharacterToMovie(ApiUtils.getUUIDFromString(movieId), character))
                    .collect(Collectors.toUnmodifiableSet()));

        var characterSaved = characterRepository.save(character);
        logger.info("Character entity saved with name {} and ID {}", characterSaved.getName(), characterSaved.getId().toString());
    }

    @Override
    @Transactional(rollbackFor = {
            InvalidUUIDFormatException.class,
            EntityExistsException.class,
            EntityNotFoundException.class,
            InvalidParameterException.class
    })
    public CharacterResponseDto updateCharacter(String id, CharacterUpdateRequestDto updateRequestDto) {
        if (Objects.isNull(id))
            throw new InvalidParameterException("Invalid parameter provided: Character to update");
        Character characterToUpdate = characterRepository.findById(ApiUtils.getUUIDFromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Character not found for ID %s".formatted(id)));

        // update values of the current character found
        characterToUpdate.setName(updateRequestDto.name());
        characterToUpdate.setImage(updateRequestDto.image());
        characterToUpdate.setAge(updateRequestDto.age());
        characterToUpdate.setWeight(updateRequestDto.weight());
        characterToUpdate.setHistory(updateRequestDto.history());

        // takes the movies where the character appears
        if (!CollectionUtils.isEmpty(updateRequestDto.moviesWhereAppears()))
            characterToUpdate.getMovies().addAll(updateRequestDto.moviesWhereAppears().stream()
                    .map(movieId -> movieService.appendCharacterToMovie(
                            ApiUtils.getUUIDFromString(movieId), characterToUpdate))
                    .collect(Collectors.toUnmodifiableSet()));

        // remove the movies where the current character doesn't appear
        if (!CollectionUtils.isEmpty(updateRequestDto.moviesToUnlink()))
            characterToUpdate.getMovies().removeAll(updateRequestDto.moviesToUnlink().stream()
                    .map(movieId -> movieService.removeCharacterFromMovie(
                            ApiUtils.getUUIDFromString(movieId), characterToUpdate))
                    .collect(Collectors.toUnmodifiableSet()));
        return characterMapper.toDTO(characterRepository.save(characterToUpdate));
    }

    @Override
    @Transactional(rollbackFor = {
            InvalidUUIDFormatException.class,
            EntityNotFoundException.class,
            InvalidParameterException.class
    })
    public void deleteCharacter(String id) {
        if (Objects.isNull(id))
            throw new InvalidParameterException("The provided ID is invalid or null");
        Character characterToDelete = characterRepository.findById(ApiUtils.getUUIDFromString(id))
                .orElseThrow(() -> new EntityNotFoundException("Character not found for ID %s".formatted(id)));
        characterRepository.delete(characterToDelete);
        logger.info("The character with ID {} was deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CharacterResponseDto> listCharacters(int pageNumber, String characterName,
                                                     int age, Set<String> moviesName) {
        Pageable pageable = PageRequest.of(pageNumber, 10);
        pageable.next().getPageNumber();
        return characterRepository.findAll(characterSpec.getByFilters(characterName, age, moviesName), pageable)
                .map(characterMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CharacterResponseDto getCharacterById(String id) {
        if (Objects.isNull(id))
            throw new InvalidParameterException("Invalid parameter value: characterId");
        return characterRepository.findById(ApiUtils.getUUIDFromString(id)).map(characterMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Character not found for ID %s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Character getCharacterById(UUID id) {
        if (Objects.isNull(id))
            throw new InvalidParameterException("Invalid parameter value: characterId");
        return characterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Character not found for ID %s".formatted(id)));
    }
}
