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

    @GetMapping("{id}")
    public ResponseEntity<MovieResponse> getOne(@PathVariable Long id) {
        return ResponseEntity
                .ok().body(service.getResponseById(id));
    }

    @GetMapping("movies")
    public ResponseEntity<MovieBasicResponseList> getList() {
        return ResponseEntity
                .ok().body(service.getAll());
    }

    //TODO delete a movie
}
