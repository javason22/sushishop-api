package com.sushishop.service;

import com.sushishop.entity.Status;
import com.sushishop.repository.StatusRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class StatusService {

    private final StatusRepository statusRepository;

    @Cacheable(value = "status", key = "#name", unless = "#result == null || #name == null")
    public Status findByName(String name) {
        return statusRepository.findByName(name).stream().findFirst().orElseThrow(() -> new EntityNotFoundException("Status not found"));
    }

    /*@Cacheable(value = "status", key = "'id-' + #id")
    public Status findById(Integer id) {
        return statusRepository.findById(id).orElse(null);
    }

    public Integer getIdByName(String name) {
        Status status = findByName(name);
        return status == null ? null : status.getId();
    }*/
}
