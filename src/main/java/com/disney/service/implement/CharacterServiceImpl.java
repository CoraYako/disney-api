package com.disney.service.implement;

import com.disney.model.dto.request.CharacterRequestDto;
import com.disney.model.dto.request.CharacterUpdateRequestDto;
import com.disney.model.dto.response.CharacterResponseDto;
import com.disney.model.entity.Character;
import com.disney.model.mapper.CharacterMapper;
import com.disney.repository.CharacterRepository;
import com.disney.repository.specification.CharacterSpecification;
import com.disney.service.CharacterService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Validated
public class CharacterServiceImpl implements CharacterService {
    private final CharacterMapper characterMapper;
    private final CharacterRepository characterRepository;
    private final CharacterSpecification characterSpec;

    public CharacterServiceImpl(CharacterMapper characterMapper,
                                CharacterRepository characterRepository,
                                CharacterSpecification characterSpec) {
        this.characterMapper = characterMapper;
        this.characterRepository = characterRepository;
        this.characterSpec = characterSpec;
    }

    @Override
    @Transactional(rollbackFor = {IllegalArgumentException.class, EntityExistsException.class})
    public void createCharacter(CharacterRequestDto requestDto) {
        if (Objects.isNull(requestDto) || !StringUtils.hasLength(requestDto.name()))
            throw new IllegalArgumentException("Invalid argument passed: Character object");
        if (characterRepository.existsByName(requestDto.name()))
            throw new EntityExistsException("The character %s already exist".formatted(requestDto.name()));
        Character character = characterMapper.toEntity(requestDto);
        characterRepository.save(character);
    }

    @Override
    @Transactional(rollbackFor = {
            IllegalArgumentException.class,
            EntityExistsException.class,
            EntityNotFoundException.class
    })
    public CharacterResponseDto updateCharacter(UUID id, CharacterUpdateRequestDto updateRequestDto) {
        if (Objects.isNull(id) || Objects.isNull(updateRequestDto) || !StringUtils.hasLength(updateRequestDto.name()))
            throw new IllegalArgumentException("Invalid parameter provided: Character to update");
        if (characterRepository.existsByName(updateRequestDto.name()))
            throw new EntityExistsException("The character %s already exists".formatted(updateRequestDto.name()));
        Character character = characterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Character not found for ID %s".formatted(id)));
        // update values of the current founded character
        character.setImage(updateRequestDto.image());
        character.setName(updateRequestDto.name());
        character.setAge(updateRequestDto.age());
        character.setWeight(updateRequestDto.weight());
        character.setHistory(updateRequestDto.history());

        // TODO: 4/11/2023 find movies if the list of moviesId aren't null

        return characterMapper.toDTO(characterRepository.save(character));
    }

    @Override
    @Transactional(rollbackFor = {IllegalArgumentException.class, EntityNotFoundException.class})
    public void deleteCharacter(UUID id) {
        if (Objects.isNull(id))
            throw new IllegalArgumentException("The provided ID is invalid or null");
        Character characterToDelete = characterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Character not found for ID %s".formatted(id)));
        characterRepository.delete(characterToDelete);
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
    public CharacterResponseDto getCharacterDtoById(UUID id) {
        if (Objects.isNull(id))
            throw new IllegalArgumentException("Invalid parameter value: characterId");
        return characterRepository.findById(id).map(characterMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Character not found for ID %s".formatted(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Character getCharacterById(UUID id) {
        if (Objects.isNull(id))
            throw new IllegalArgumentException("Invalid parameter value: characterId");
        return characterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Character not found for ID %s".formatted(id)));
    }
}
