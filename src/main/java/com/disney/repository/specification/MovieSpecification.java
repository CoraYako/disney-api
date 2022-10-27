package com.disney.repository.specification;

import com.disney.model.entity.CharacterEntity;
import com.disney.model.entity.GenreEntity;
import com.disney.model.entity.MovieEntity;
import com.disney.model.request.MovieFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class MovieSpecification {

    public Specification<MovieEntity> getByFilters(MovieFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasLength(filterRequest.getTitle())) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), '%' + filterRequest.getTitle() + '%'));
            }
            if (filterRequest.getGenre() != null) {
                Join<GenreEntity, MovieEntity> join = root.join("genre", JoinType.LEFT);
                Expression<String> genreId = join.get("id");
                predicates.add(genreId.in(filterRequest.getGenre()));
            }
            query.distinct(true);
            String orderByField = "creation";
            query.orderBy(
                    filterRequest.isASC() ?
                            criteriaBuilder.asc(root.get(orderByField)) :
                            criteriaBuilder.desc(root.get(orderByField))
            );
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
