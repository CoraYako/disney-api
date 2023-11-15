package com.disney.controller;

import com.disney.model.dto.request.GenreRequestDto;
import com.disney.model.dto.request.GenreUpdateRequestDto;
import com.disney.model.dto.response.GenreResponseDto;
import com.disney.service.GenreService;
import com.disney.util.ApiUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiUtils.GENRE_BASE_URL)
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

    @PatchMapping(ApiUtils.GENRE_URI_VARIABLE)
    public ResponseEntity<GenreResponseDto> updateGenreById(@PathVariable String genreId,
                                                            @RequestBody GenreUpdateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(genreService.updateGenre(genreId, requestDto));
    }

    @GetMapping(ApiUtils.GENRE_URI_VARIABLE)
    public ResponseEntity<GenreResponseDto> getGenre(@PathVariable String genreId) {
        return ResponseEntity.status(HttpStatus.OK).body(genreService.getGenreById(genreId));
    }

    @GetMapping
    public ResponseEntity<Page<GenreResponseDto>> listGenres(
            @RequestParam(required = false, defaultValue = "0", name = "page") int pageNumber) {
        return ResponseEntity.status(HttpStatus.OK).body(genreService.listMovieGenres(pageNumber));
    }
}
