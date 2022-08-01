package com.disney.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CharacterDTO {

    private String id;

    private String image;

    private Integer age;

    private Double weight;

    private String history;

    private List<MovieDTO> movies;

}
