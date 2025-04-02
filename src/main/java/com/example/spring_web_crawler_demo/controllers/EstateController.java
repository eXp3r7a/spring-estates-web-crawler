package com.example.spring_web_crawler_demo.controllers;

import com.example.spring_web_crawler_demo.repositories.EstateRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/estates")
public class EstateController {

    private final EstateRepository estateRepository;

    public EstateController(EstateRepository estateRepository){
        this.estateRepository = estateRepository;
    }

    @GetMapping("/get")
    public String getAllEstates(Model model){
        model.addAttribute("estates", estateRepository.findAll());
        return "table/get_table";
    }
}
