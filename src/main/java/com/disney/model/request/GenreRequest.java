package com.disney.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
public class GenreRequest {

    @NotBlank(message = "The name can't be whitespaces")
    @NotEmpty(message = "The name can't be empty or null")
    private String name;

    @NotBlank(message = "The image can't be whitespaces")
    @NotEmpty(message = "The image can't be empty or null")
    private String image;
}
