package com.disney.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovieDTO {

    private String id;

    private String image;

    private String title;

    private LocalDate creation;

    private Integer rate;

    private List<CharacterDTO> characters;

    private GenreDTO genre;

}
