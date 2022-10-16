package com.disney.model.response.basic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GenreBasicResponse {

    private Long id;

    private String name;

    private String image;
}
