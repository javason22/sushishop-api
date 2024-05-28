package com.sushishop.entity;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
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
public class SushiOrder {
    

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status_id", nullable = false, columnDefinition = "int default 1")
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Integer statusId;

    @Column(name = "sushi_id", nullable = false, columnDefinition = "int default 1")
    @JoinColumn(name = "sushi_id", referencedColumnName = "id")
    private Long sushiId;

    @Column(name = "createdat", nullable = false, columnDefinition = "timestamp default current_timestamp")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createdAt;

    public SushiOrder() {
    }
}
