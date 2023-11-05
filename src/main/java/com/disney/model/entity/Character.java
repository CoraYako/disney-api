package com.disney.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "characters")
@SQLDelete(sql = "UPDATE characters SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class Character implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String image;
    private String name;
    private int age;
    private double weight;
    private String history;
    @ManyToMany(mappedBy = "characters", fetch = FetchType.EAGER)
    private Set<Movie> movies;
    private boolean deleted = false;

    public UUID getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public double getWeight() {
        return weight;
    }

    public String getHistory() {
        return history;
    }

    public Set<Movie> getMovies() {
        return Objects.isNull(movies) ? movies = new HashSet<>() : movies;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setImage(String value) {
        if (!Objects.isNull(value) && !value.trim().isEmpty())
            this.image = value;
    }

    public void setName(String value) {
        if (!Objects.isNull(value) && !value.trim().isEmpty())
            this.name = value;
    }

    public void setAge(int value) {
        if (value > 0)
            this.age = value;
    }

    public void setWeight(double value) {
        if (value > 0)
            this.weight = value;
    }

    public void setHistory(String value) {
        if (!Objects.isNull(value) && !value.trim().isEmpty())
            this.history = value;
    }

    public static CharacterBuilder builder() {
        return new CharacterBuilder();
    }

    public static class CharacterBuilder {
        private UUID id;
        private String image;
        private String name;
        private int age;
        private double weight;
        private String history;
        private boolean deleted;
        private Set<Movie> movies;

        public CharacterBuilder id(UUID value) {
            this.id = value;
            return this;
        }

        public CharacterBuilder image(String value) {
            this.image = value;
            return this;
        }

        public CharacterBuilder name(String value) {
            this.name = value;
            return this;
        }

        public CharacterBuilder age(int value) {
            this.age = value;
            return this;
        }

        public CharacterBuilder weight(double value) {
            this.weight = value;
            return this;
        }

        public CharacterBuilder history(String value) {
            this.history = value;
            return this;
        }

        public CharacterBuilder deleted(boolean value) {
            this.deleted = value;
            return this;
        }

        public CharacterBuilder movies(Set<Movie> value) {
            this.movies = value;
            return this;
        }

        public Character build() {
            Character character = new Character();
            character.id = this.id;
            character.image = this.image;
            character.name = this.name;
            character.age = this.age;
            character.weight = this.weight;
            character.history = this.history;
            character.deleted = this.deleted;
            character.movies = this.movies;
            return character;
        }
    }
}
