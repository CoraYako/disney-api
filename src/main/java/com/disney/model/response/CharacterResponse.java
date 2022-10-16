package com.disney.model.response;

import com.disney.model.response.basic.MovieBasicResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CharacterResponse {

    private Long id;

    private String image;

    private String name;

    private Integer age;

    private Double weight;

    private String history;

    private List<MovieBasicResponse> movies;
}
