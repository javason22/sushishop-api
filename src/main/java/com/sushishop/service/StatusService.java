package com.sushishop.service;

import com.sushishop.entity.Status;
import com.sushishop.repository.StatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class StatusService {

    private final StatusRepository statusRepository;

    @Cacheable(value = "statuses", key = "'name-' + #name")
    public Status findByName(String name) {
        return statusRepository.findByName(name).stream().findFirst().orElse(null);
    }

    @Cacheable(value = "statuses", key = "'id-' + #id")
    public Status findById(Integer id) {
        return statusRepository.findById(id).orElse(null);
    }
}
