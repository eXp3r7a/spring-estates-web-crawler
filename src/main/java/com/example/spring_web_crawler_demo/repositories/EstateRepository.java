package com.example.spring_web_crawler_demo.repositories;

import com.example.spring_web_crawler_demo.entities.Estate;
import org.springframework.data.repository.CrudRepository;

public interface EstateRepository extends CrudRepository<Estate, Long> {
}
