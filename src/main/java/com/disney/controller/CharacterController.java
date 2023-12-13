package com.disney.controller;

import com.disney.model.dto.request.CharacterRequestDto;
import com.disney.model.dto.request.CharacterUpdateRequestDto;
import com.disney.model.dto.response.CharacterResponseDto;
import com.disney.service.CharacterService;
import com.disney.util.ApiUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping(ApiUtils.CHARACTER_BASE_URL)
public class CharacterController {
    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @PostMapping
    public ResponseEntity<Void> createMovieCharacter(@Valid @RequestBody CharacterRequestDto requestDto) {
        characterService.createCharacter(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping(ApiUtils.CHARACTER_URI_VARIABLE)
    public ResponseEntity<CharacterResponseDto> updateCharacter(@PathVariable String characterId,
                                                                @RequestBody CharacterUpdateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(characterService.updateCharacter(characterId, requestDto));
    }

    @GetMapping(ApiUtils.CHARACTER_URI_VARIABLE)
    public ResponseEntity<CharacterResponseDto> getCharacter(@PathVariable String characterId) {
        return ResponseEntity.status(HttpStatus.OK).body(characterService.getCharacterById(characterId));
    }

    @GetMapping
    public ResponseEntity<Page<CharacterResponseDto>> listMovieCharacters(
            @RequestParam(required = false, defaultValue = "0", name = "page") int pageNumber,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "0") int age,
            @RequestParam(required = false) Set<String> moviesId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(characterService.listCharacters(pageNumber, name, age, moviesId));
    }

    @DeleteMapping(ApiUtils.CHARACTER_URI_VARIABLE)
    public ResponseEntity<Void> deleteCharacter(@PathVariable String characterId) {
        characterService.deleteCharacter(characterId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
