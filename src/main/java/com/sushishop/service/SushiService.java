package com.sushishop.service;

import com.sushishop.entity.Sushi;
import com.sushishop.repository.SushiRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class SushiService {

    private final SushiRepository sushiRepository;

    @Cacheable(value = "sushi", key = "#name", unless = "#result == null")
    public Sushi getSushiByName(String name) {
        List<Sushi> sushiList = sushiRepository.findByName(name);
        if (sushiList.isEmpty()) {
            throw new EntityNotFoundException("Sushi not found");
        }
        return sushiList.get(0);
    }

    /*@Cacheable(value = "sushi", key = "#id", unless = "#result == null")
    public Sushi getSushiById(Integer id) {
        return sushiRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Sushi not found"));
    }*/
}
