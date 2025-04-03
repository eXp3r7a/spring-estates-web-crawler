package com.example.spring_web_crawler_demo.repositories;

import com.example.spring_web_crawler_demo.entities.Estate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstateRepository extends CrudRepository<Estate, Long> {
    @Query("SELECT e from Estate e WHERE " +
            "LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.area) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.price) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.listingUrl) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Estate> searchEstates(String keyword);
}
