package com.disney.model.response.basic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CharacterBasicResponse {

    private Long id;

    private String image;

    private String name;
}
