package com.disney.model.response;

import com.disney.model.response.basic.MovieBasicResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GenreResponse {

    private Long id;

    private String name;

    private String image;
}
