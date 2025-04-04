package com.example.spring_web_crawler_demo.controllers;


import com.example.spring_web_crawler_demo.services.CrawlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CrawlerController {

    private final CrawlerService crawlerService;

    public CrawlerController(CrawlerService crawlerService){
        this.crawlerService = crawlerService;
    }
    private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);

    @GetMapping("/start-crawl")
    public String startCrawler() {
        return crawlerService.startCrawler();
    }
}
