package com.sushishop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sushishop.entity.Status;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer>{

    /**
     * Find status by name
     *
     * @param name status name
     * @return List<Status>
     */
    List<Status> findByName(String name);
}
