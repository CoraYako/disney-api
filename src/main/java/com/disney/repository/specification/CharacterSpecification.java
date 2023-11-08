package com.disney.repository.specification;

import com.disney.model.entity.Character;
import com.disney.model.entity.Movie;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CharacterSpecification {

    public Specification<Character> getByFilters(String characterName, int age, Set<String> moviesName) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasLength(characterName))
                predicates.add(criteriaBuilder
                        .like(criteriaBuilder.lower(root.get("name")), '%' + characterName + '%')
                );

            if (age > 0)
                predicates.add(criteriaBuilder.equal(root.get("age"), age));

            if (!CollectionUtils.isEmpty(moviesName)) {
                Join<Movie, Character> join = root.join("movies", JoinType.INNER);
                Expression<String> movieId = join.get("id");
                predicates.add(movieId.in(moviesName));
            }
            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
