package com.sushishop.service;

import com.sushishop.entity.Sushi;
import com.sushishop.repository.SushiRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class SushiService {

    private final SushiRepository sushiRepository;

    public Sushi getSushiByName(String name) {
        List<Sushi> sushiList = sushiRepository.findByName(name);
        if (sushiList.isEmpty()) {
            return null;
        }
        return sushiList.get(0);
    }
}
