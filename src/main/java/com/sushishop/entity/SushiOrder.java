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

@Entity
@Table(name = "sushi_order")
public class SushiOrder {
    

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status_id")
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private Integer statusId;

    @Column(name = "sushi_id")
    @JoinColumn(name = "sushi_id", referencedColumnName = "id")
    private Long sushiId;

    @Column(name = "createdat")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private Timestamp createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Long getSushiId() {
        return sushiId;
    }

    public void setSushiId(Long sushiId) {
        this.sushiId = sushiId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    
}
