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

    //private final MyCrawler myCrawler;
    private final EstateRepository estateRepository;

    public CrawlerController(EstateRepository estateRepository){
        this.estateRepository = estateRepository;
    }
    private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);

    @GetMapping("/start-crawl")
    public String startCrawler() {
        try {
            // Step 1: Set up the crawl configuration
            CrawlConfig config = new CrawlConfig();
            config.setCrawlStorageFolder("/tmp/crawler/"); // Temporary storage folder
            config.setPolitenessDelay(5000);
            config.setMaxDepthOfCrawling(2); // Limit the depth of crawling
            config.setMaxPagesToFetch(50); // Limit the number of pages to fetch

            CrawlConfig config2 = new CrawlConfig();
            config2.setCrawlStorageFolder("/tmp/crawler2/"); // Temporary storage folder
            config2.setPolitenessDelay(5000);
            config2.setMaxDepthOfCrawling(2); // Limit the depth of crawling
            config2.setMaxPagesToFetch(50); // Limit the number of pages to fetch


            // Step 2: Initialize PageFetcher
            PageFetcher pageFetcher = new PageFetcher(config);
            PageFetcher pageFetcher2 = new PageFetcher(config2);

            // Step 3: Set up RobotstxtServer
            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

            // Step 4: Create and configure CrawlController
            CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
            CrawlController controller2 = new CrawlController(config2, pageFetcher2, robotstxtServer);

            List<String> crawler1Domain = ImmutableList.of("https://www.olx.bg/q-imoti/");
            List<String> crawler2Domain = ImmutableList.of("https://www.alo.bg/obiavi/imoti-prodajbi/apartamenti-stai/");

            // Add seed URLs (starting points for the crawler)
            controller.addSeed("https://www.olx.bg/q-imoti/");
            controller2.addSeed("https://www.alo.bg/obiavi/imoti-prodajbi/apartamenti-stai/");

            // Step 5: Start the crawl using your CustomCrawler class
            CrawlController.WebCrawlerFactory<CrawlerService> factory1 = () -> new CrawlerService(crawler1Domain, estateRepository);
            CrawlController.WebCrawlerFactory<CrawlerService> factory2 = () -> new CrawlerService(crawler2Domain, estateRepository);

            // The first crawler will have 5 concurrent threads and the second crawler will have 7 threads.
            controller.startNonBlocking(factory1, 4);
            controller2.startNonBlocking(factory2, 5);

            controller.waitUntilFinish();
            logger.info("Crawler 1 is finished.");

            controller2.waitUntilFinish();
            logger.info("Crawler 2 is finished.");
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to start crawler: " + e.getMessage();
        }
        return "Crawling has started!";
    }
}
