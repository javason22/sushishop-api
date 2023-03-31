package com.sushishop.entity;

import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;

@Entity
@Table(name = "sushi")
public class Sushi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("sushi_name")
    private String name;

    private int timeToMake;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTimeToMake() {
        return timeToMake;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTimeToMake(int timeToMake) {
        this.timeToMake = timeToMake;
    }
}
