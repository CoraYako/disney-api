package com.disney.model.request;

import com.disney.model.response.MovieResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CharacterRequest {

    @NotEmpty(message = "The image can't be empty or null")
    @NotBlank(message = "The image can't be whitespaces")
    private String image;

    @NotEmpty(message = "The name can't be empty or null")
    @NotBlank(message = "The name can't be whitespaces")
    private String name;

    @NotNull(message = "The age can't be null")
    @Min(value = 1, message = "Positive numbers only, minimum is 1")
    private Integer age;

    @NotNull(message = "The weight can't be null")
    @Min(value = 1, message = "Positive numbers only, minimum is 1")
    private Double weight;

    @NotEmpty(message = "The history can't be empty or null")
    @NotBlank(message = "The history can't be whitespaces")
    private String history;

    private List<MovieResponse> moviesId;
}
