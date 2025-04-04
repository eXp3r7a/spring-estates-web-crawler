package com.example.spring_web_crawler_demo.controllers;

import com.example.spring_web_crawler_demo.repositories.EstateRepository;
import com.example.spring_web_crawler_demo.services.CrawlerService;
import com.google.common.collect.ImmutableList;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
