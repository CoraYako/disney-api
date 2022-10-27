package com.disney.controller;

import com.disney.model.request.CharacterRequest;
import com.disney.model.response.CharacterResponse;
import com.disney.model.response.basic.CharacterBasicResponseList;
import com.disney.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/character")
public class CharacterController {

    private final CharacterService service;

    @PostMapping("save")
    public ResponseEntity<CharacterResponse> save(@Valid @RequestBody CharacterRequest request) {
        CharacterResponse response = service.save(request);
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/save").toString());
        return ResponseEntity
                .created(uri).body(response);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<CharacterResponse> updateById(@PathVariable Long id, @RequestBody CharacterRequest request) {
        CharacterResponse response = service.update(id, request);
        return ResponseEntity
                .ok().body(response);
    }

    @GetMapping("{id}")
    public ResponseEntity<CharacterResponse> getOne(@PathVariable Long id) {
        return ResponseEntity
                .ok().body(service.getResponseById(id));
    }

    @GetMapping("characters")
    public ResponseEntity<CharacterBasicResponseList> getList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) List<Long> movies) {
        return ResponseEntity
                .ok().body(service.getByFilters(name, age, movies));
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity
                .noContent().build();
    }
}
