package com.disney.model.entity;


import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "genres")
public class GenreEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String image;
    @OneToMany(mappedBy = "genre", cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<MovieEntity> movies;
}
