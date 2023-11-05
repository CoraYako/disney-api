package com.disney.controller;

import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.request.GenreUpdateRequestDto;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/genres")
public class GenreController {
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @PostMapping
    public ResponseEntity<Void> createMovieGenre(@Valid @RequestBody GenreRequestDto requestDto) {
        genreService.createGenre(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{genreId}")
    public ResponseEntity<GenreResponseDto> updateGenreById(@PathVariable String genreId,
                                                            @RequestBody GenreUpdateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(genreService.updateGenre(genreId, requestDto));
    }

    @GetMapping("/{genreId}")
    public ResponseEntity<GenreResponseDto> getGenre(@PathVariable String genreId) {
        return ResponseEntity.status(HttpStatus.OK).body(genreService.getGenreById(genreId));
    }
}
