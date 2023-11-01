package com.disney.model.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "characters")
@SQLDelete(sql = "UPDATE characters SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class CharacterEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String image;
    private String name;
    private Integer age;
    private Double weight;
    private String history;
    @ManyToMany(mappedBy = "characters", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<MovieEntity> movies;
    private boolean deleted = Boolean.FALSE;
}
