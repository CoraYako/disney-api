package com.disney.model.entity;


import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "genres")
public class Genre implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    @OneToMany(mappedBy = "genre", fetch = FetchType.LAZY)
    private Set<Movie> movies;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Movie> getMovies() {
        return Objects.isNull(movies) ? movies = new HashSet<>() : movies;
    }

    public static GenreBuilder builder() {
        return new GenreBuilder();
    }

    public static class GenreBuilder {
        private UUID id;
        private String name;
        private Set<Movie> movies;

        public GenreBuilder id(UUID value) {
            this.id = value;
            return this;
        }

        public GenreBuilder name(String value) {
            this.name = value;
            return this;
        }

        public GenreBuilder movies(Set<Movie> value) {
            this.movies = value;
            return this;
        }

        public Genre build() {
            Genre genre = new Genre();
            genre.id = this.id;
            genre.name = this.name;
            genre.movies = this.movies;
            return genre;
        }
    }
}
