package com.sushishop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sushishop.entity.SushiOrder;

@Repository
public interface SushiOrderRepository extends JpaRepository<SushiOrder, Long>{


}
