package com.disney.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "characters")
@SQLDelete(sql = "UPDATE characters SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class CharacterEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String image;
    private String name;
    private int age;
    private double weight;
    private String history;
    @ManyToMany(mappedBy = "characters", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<MovieEntity> movies;
    private boolean deleted = Boolean.FALSE;
}
