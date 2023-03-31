package com.sushishop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sushishop.entity.Status;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long>{

    List<Status> findByName(String name);
}
