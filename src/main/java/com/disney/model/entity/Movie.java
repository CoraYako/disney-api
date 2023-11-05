package com.disney.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "movies")
@SQLDelete(sql = "UPDATE movies SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class Movie implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String image;
    private String title;
    @Column(name = "creation_date")
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate creationDate;
    private int rate;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "genre_id")
    private Genre genre;
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name = "rel_movie_character",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "character_id"))
    private Set<Character> characters;
    private boolean deleted = false;

    public UUID getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public int getRate() {
        return rate;
    }

    public Genre getGenre() {
        return genre;
    }

    public Set<Character> getCharacters() {
        return Objects.isNull(characters) ? characters = new HashSet<>() : characters;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setImage(String value) {
        if (!Objects.isNull(value) && !value.trim().isEmpty())
            this.image = value;
    }

    public void setTitle(String value) {
        if (!Objects.isNull(value) && !value.trim().isEmpty())
            this.title = value;
    }

    public void setCreationDate(LocalDate value) {
        if (!Objects.isNull(value))
            this.creationDate = value;
    }

    public void setRate(int value) {
        if (value > 0)
            this.rate = value;
    }

    public void setGenre(Genre value) {
        if (!Objects.isNull(value))
            this.genre = value;
    }

    public static MovieBuilder builder() {
        return new MovieBuilder();
    }

    public static class MovieBuilder {
        private UUID id;
        private String image;
        private String title;
        private LocalDate creationDate;
        private int rate;
        private Genre genre;
        private boolean deleted;
        private Set<Character> characters;

        public MovieBuilder id(UUID value) {
            this.id = value;
            return this;
        }

        public MovieBuilder image(String value) {
            this.image = value;
            return this;
        }

        public MovieBuilder title(String value) {
            this.title = value;
            return this;
        }

        public MovieBuilder creationDate(LocalDate value) {
            this.creationDate = value;
            return this;
        }

        public MovieBuilder rate(int value) {
            this.rate = value;
            return this;
        }

        public MovieBuilder genre(Genre value) {
            this.genre = value;
            return this;
        }

        public MovieBuilder deleted(boolean value) {
            this.deleted = value;
            return this;
        }

        public MovieBuilder characters(Set<Character> value) {
            this.characters = value;
            return this;
        }

        public Movie build() {
            Movie movie = new Movie();
            movie.id = this.id;
            movie.image = this.image;
            movie.title = this.title;
            movie.creationDate = this.creationDate;
            movie.rate = this.rate;
            movie.genre = this.genre;
            movie.deleted = this.deleted;
            movie.characters = this.characters;
            return movie;
        }
    }
}
