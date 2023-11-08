package com.disney.controller;

import com.disney.model.dto.request.MovieRequestDto;
import com.disney.model.dto.request.MovieUpdateRequestDto;
import com.disney.model.dto.response.MovieResponseDto;
import com.disney.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<Void> createMovie(@Valid @RequestBody MovieRequestDto requestDto) {
        movieService.createMovie(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{movieId}")
    public ResponseEntity<MovieResponseDto> updateMovie(@PathVariable String movieId,
                                                        @RequestBody MovieUpdateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(movieService.updateMovie(movieId, requestDto));
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponseDto> getMovie(@PathVariable String movieId) {
        return ResponseEntity.status(HttpStatus.OK).body(movieService.getMovieById(movieId));
    }

    @GetMapping
    public ResponseEntity<Page<MovieResponseDto>> listMovies(
            @RequestParam(required = false, defaultValue = "0", name = "page") int pageNumber,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false, defaultValue = "ASC") String order) {
        return ResponseEntity.status(HttpStatus.OK).body(movieService.listMovies(pageNumber, title, genre, order));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteMovie(@PathVariable String movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
