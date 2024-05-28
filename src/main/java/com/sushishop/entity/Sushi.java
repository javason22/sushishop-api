package com.sushishop.entity;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@Table(name = "sushi")
public class Sushi {
    
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    @JsonProperty("sushi_name")
    private String name;

    @Column(name = "time_to_make", nullable = false, columnDefinition = "int default 0")
    private int timeToMake;

    public Sushi() {
    }

}
