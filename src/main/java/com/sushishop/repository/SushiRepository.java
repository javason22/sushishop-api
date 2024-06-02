package com.sushishop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sushishop.entity.Sushi;

@Repository
public interface SushiRepository extends JpaRepository<Sushi, Integer>{

    /**
     * Find sushi by name
     *
     * @param name sushi name
     * @return Sushi with the given name
     */
    Sushi findTopByName(String name);
}
