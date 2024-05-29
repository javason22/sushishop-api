package com.sushishop.entity;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@Entity
@Table(name = "sushi", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "sushi_name_unique" )})
public class Sushi implements Serializable {

    @Serial
    private static final long serialVersionUID = 9085093458723L;

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    @JsonProperty("sushi_name")
    private String name;

    @Column(name = "time_to_make", nullable = false, columnDefinition = "int default 0")
    private int timeToMake;

    @Tolerate
    public Sushi() {
    }

}
