package com.disney.controller;

import com.disney.model.request.MovieRequest;
import com.disney.model.response.MovieResponse;
import com.disney.model.response.basic.MovieBasicResponseList;
import com.disney.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movie")
public class MovieController {

    private final MovieService service;

    @PostMapping("save")
    public ResponseEntity<MovieResponse> save(@Valid @RequestBody MovieRequest request) {
        MovieResponse response = service.save(request);
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/save").toString());
        return ResponseEntity
                .created(uri).body(response);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<MovieResponse> update(@PathVariable Long id, @RequestBody MovieRequest request) {
        MovieResponse response = service.update(id, request);
        return ResponseEntity
                .ok().body(response);
    }

    @PutMapping("{idMovie}/characters/{idCharacter}")
    public ResponseEntity<MovieResponse> addCharacter(@PathVariable Long idMovie, @PathVariable Long idCharacter) {
        MovieResponse response = service.addCharacter(idMovie, idCharacter);
        return ResponseEntity
                .ok().body(response);
    }

    @DeleteMapping("{idMovie}/characters/{idCharacter}")
    public ResponseEntity<MovieResponse> removeCharacter(@PathVariable Long idMovie, @PathVariable Long idCharacter) {
        MovieResponse response = service.removeCharacter(idMovie, idCharacter);
        return ResponseEntity
                .ok().body(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<MovieResponse> getOne(@PathVariable Long id) {
        return ResponseEntity
                .ok().body(service.getResponseById(id));
    }

    @GetMapping("movies")
    public ResponseEntity<MovieBasicResponseList> getList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long genre,
            @RequestParam(required = false, defaultValue = "ASC") String order
            ) {
        return ResponseEntity
                .ok().body(service.getByFilters(title, genre, order));
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity
                .noContent().build();
    }
}
