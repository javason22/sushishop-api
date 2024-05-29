package com.sushishop.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Tolerate;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@Entity
@EqualsAndHashCode
@Table(name = "status", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "status_name_unique" )})
public class Status implements Serializable {

    @Serial
    private static final long serialVersionUID = 893740950L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Tolerate
    public Status() {
    }
    
}
