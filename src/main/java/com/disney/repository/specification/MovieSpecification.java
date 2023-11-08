package com.disney.repository.specification;

import com.disney.model.entity.Genre;
import com.disney.model.entity.Movie;
import com.disney.util.ApiUtils;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class MovieSpecification {

    public Specification<Movie> getByFilters(String title, String genre, String order) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasLength(title)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), '%' + title + '%'));
            }
            if (StringUtils.hasLength(genre)) {
                Join<Genre, Movie> join = root.join("genre", JoinType.LEFT);
                Expression<String> genreId = join.get("genre_id");
                predicates.add(genreId.in(genre));
            }
            query.distinct(true);
            String orderByField = "creationDate";
            query.orderBy(
                    ApiUtils.isASC(order) ?
                            criteriaBuilder.asc(root.get(orderByField))
                            :
                            criteriaBuilder.desc(root.get(orderByField))
            );
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
