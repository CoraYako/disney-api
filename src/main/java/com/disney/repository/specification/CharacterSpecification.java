package com.disney.repository.specification;

import com.disney.model.entity.CharacterEntity;
import com.disney.model.entity.MovieEntity;
import com.disney.model.request.CharacterFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class CharacterSpecification {

    public Specification<CharacterEntity> getByFilters(CharacterFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasLength(filterRequest.getName())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), '%' + filterRequest.getName() + '%'));
            }
            if (filterRequest.getAge() != null) {
                predicates.add(criteriaBuilder.equal(root.get("age"), filterRequest.getAge()));
            }
            if (!CollectionUtils.isEmpty(filterRequest.getMovies())) {
                Join<MovieEntity, CharacterEntity> join = root.join("movies", JoinType.INNER);
                Expression<String> movieId = join.get("id");
                predicates.add(movieId.in(filterRequest.getMovies()));
            }
            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
