package com.disney.model.mapper;

import com.disney.model.entity.GenreEntity;
import com.disney.model.request.GenreRequest;
import com.disney.model.response.GenreResponse;
import com.disney.model.response.basic.GenreBasicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenreMapper {

    public GenreEntity DTO2Entity(GenreRequest request) {
        GenreEntity entity = new GenreEntity();
        entity.setImage(request.getImage());
        entity.setName(request.getName());
        return entity;
    }

    public GenreResponse entity2DTO(GenreEntity entity) {
        GenreResponse response = new GenreResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setImage(entity.getImage());
        return response;
    }

    public GenreBasicResponse entity2BasicDTO(GenreEntity entity) {
        GenreBasicResponse basicResponse = new GenreBasicResponse();
        basicResponse.setId(entity.getId());
        basicResponse.setName(entity.getName());
        basicResponse.setImage(entity.getImage());
        return basicResponse;
    }

    public GenreEntity refreshValues(GenreRequest request, GenreEntity entity) {
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            entity.setName(request.getName());
        }
        if (request.getImage() != null && !request.getImage().trim().isEmpty()) {
            entity.setImage(request.getImage());
        }
        return entity;
    }
}
