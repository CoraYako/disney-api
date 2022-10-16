package com.disney.model.response.basic;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class MovieBasicResponse {

    private Long id;

    private String image;

    private String title;

    private String creation;
}
