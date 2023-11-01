package com.disney.controller;

import com.disney.model.request.GenreRequest;
import com.disney.model.response.GenreResponse;
import com.disney.service.GenreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/genre")
public class GenreController {

    private final GenreService service;

    public GenreController(GenreService service) {
        this.service = service;
    }

    @PostMapping("save")
    public ResponseEntity<GenreResponse> save(@Valid @RequestBody GenreRequest request) {
        GenreResponse response = service.save(request);
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/save").toString());
        return ResponseEntity
                .created(uri).body(response);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<GenreResponse> update(@PathVariable Long id, @RequestBody GenreRequest request) {
        GenreResponse response = service.update(id, request);
        return ResponseEntity
                .ok().body(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<GenreResponse> getOne(@PathVariable Long id) {
        GenreResponse response = service.getResponseById(id);
        return ResponseEntity
                .ok().body(response);
    }
}
