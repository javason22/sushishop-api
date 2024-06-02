package com.sushishop.entity;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@AllArgsConstructor
@Entity
@EqualsAndHashCode
@Table(name = "sushi_order")
public class SushiOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 9498304833849L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "status_id", referencedColumnName = "id", nullable = false)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "sushi_id", referencedColumnName = "id", nullable = false)
    private Sushi sushi;

    @Column(name = "createdAt", nullable = false, columnDefinition = "timestamp default current_timestamp")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createdAt;

    public SushiOrder() {
    }
}
