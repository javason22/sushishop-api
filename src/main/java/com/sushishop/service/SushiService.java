package com.sushishop.service;

import com.sushishop.entity.Sushi;
import com.sushishop.repository.SushiRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class SushiService {

    private final SushiRepository sushiRepository;

    @Cacheable(value = "sushi", key = "#name", unless = "#result == null")
    public Sushi getSushiByName(String name) {
        Sushi sushi = sushiRepository.findTopByName(name);
        if (sushi == null) {
            throw new EntityNotFoundException("Sushi not found");
        }
        return sushi;
    }
}
