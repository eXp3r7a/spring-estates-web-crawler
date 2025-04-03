package com.example.spring_web_crawler_demo.services;

import com.example.spring_web_crawler_demo.entities.Estate;
import com.example.spring_web_crawler_demo.repositories.EstateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstateService {

    private final EstateRepository estateRepository;

    public EstateService(EstateRepository estateRepository){
        this.estateRepository = estateRepository;
    }

    public List<Estate> searchEstates(String keyword) {
        return estateRepository.searchEstates(keyword);
    }
}
