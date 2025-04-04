package com.example.spring_web_crawler_demo.services;
import com.example.spring_web_crawler_demo.entities.CrawledData;
import com.example.spring_web_crawler_demo.entities.Estate;
import com.example.spring_web_crawler_demo.repositories.EstateRepository;
import com.google.common.collect.ImmutableList;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CrawlerService extends WebCrawler{

    private final EstateRepository estateRepository;
    private final List<String> myCrawlDomains;

    public CrawlerService(List<String> myCrawlDomains, EstateRepository estateRepository) {
        this.myCrawlDomains = ImmutableList.copyOf(myCrawlDomains);
        this.estateRepository = estateRepository;
    }

    private static final Pattern FILTERS = Pattern.compile(
            ".*(\\.(css|js|bmp|gif|jpe?g" + "|png|tiff?|mid|mp2|mp3|mp4" +
                    "|wav|avi|mov|mpeg|ram|m4v|pdf" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");


    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (FILTERS.matcher(href).matches()) {
            return false;
        }
        for (String crawlDomain : myCrawlDomains) {
            if (href.startsWith(crawlDomain)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL(); // Get the page URL

        try {
            Thread.sleep(3000);
            // Fetch and parse the page with Jsoup
            Document document = Jsoup.connect(url).get();
            String title = document.title();
            String content = document.body().text();

            // Create Crawler entity object
            CrawledData data = new CrawledData();
            data.setUrl(url);
            data.setTitle(title);
            data.setContent(content);

            if (url.contains("imot") && !url.contains("q-къща/") && !url.contains("/ad/") && !url.contains("/ads/")){
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("debug-log.txt", true))) {
                    writer.write("Visited URL: " + url + "\n");
                    writer.write("title " + title + "\n");

                    if(url.contains("olx.bg")){
                        List<Estate> estatesOlx = handleCrawlerDataFromOlxBg(content);
                        if (!estatesOlx.isEmpty()){
                            //estateRepository.saveAll(estatesOlx);
                            /*for (Estate estate : estatesOlx){
                                writer.write("content " + estate + "\n");
                            }*/
                        }
                    }else if (url.contains("alo.bg")){
                        List<Estate> estatesAloBg = handleCrawlerDataFromAloBg(content);
                        if(!estatesAloBg.isEmpty()){
                            estateRepository.saveAll(estatesAloBg);
                            /*for(Estate estate : estatesAloBg){
                                writer.write("content " + estate + "\n");
                            }*/
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace(); // Handle exceptions
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

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

    public List<Estate> handleCrawlerDataFromOlxBg(String content){
        List<Estate> estateList = new ArrayList<>();
        content=content.replaceAll("Последвай \\s?",".\n");
        content=content.replaceAll("Промотирана обява\\s?","");
        content=content.replaceAll("Запази търсенето Ще те известим, когато има нови обяви по зададените критерии. Запази \\s?","");

        // Split each row into parts (adjust the delimiter as needed)
        String[] rows = content.split("\n");

        for (String row : rows) {
            // Split each row into parts (adjust the delimiter as needed)
            if(row.length() > 1){
                if(!row.contains("Навигиране до") && !row.contains("1 2 3 ... 25 Подобни") && !row.contains("промяна на предназначение")){
                    Estate estate = extractEstateForOlxBg(row);
                    if(estate.getTitle() != null){
                        estateList.add(estate);
                    }
                }
            }
        }
        return estateList;
    }

    private static Estate extractEstateForOlxBg(String contentRow){
        Estate estate = new Estate();
        //Title
        Pattern pattern = Pattern.compile("(.+?)\\s\\d{1,3}(?:\\s?\\d{3})*\\s(?:лв\\.|€|гр\\.\\s)");
        Matcher matcher = pattern.matcher(contentRow);
        if(matcher.find()){
            estate.setTitle(matcher.group(1).trim());
        }
        else{
            pattern = Pattern.compile("(.+?)\\s(?:гр\\.\\s)");
            matcher = pattern.matcher(contentRow);
            if(matcher.find()){
                estate.setTitle(matcher.group(1).trim());
            }
        }

        //Price
        pattern = Pattern.compile("(\\d{1,3}(?:\\s?\\d{3})*)\\s(лв\\.|€)");
        matcher = pattern.matcher(contentRow);
        if (matcher.find()){
            estate.setPrice(matcher.group(1).trim() + " " + matcher.group(2));
        }

        //Location
        if(contentRow.contains("По договаряне")){
            pattern = Pattern.compile("По договаряне\\s+(.*?)-");
        }else {
            pattern = Pattern.compile("(?:лв\\.|€|гр\\.)\\s+(.*?)-");
        }
        matcher = pattern.matcher(contentRow);
        if (matcher.find()){
            estate.setLocation(matcher.group(1).trim());

            //if missing
            if(!estate.getLocation().contains("гр.")){
                estate.setLocation("гр. " + estate.getLocation());
            }
        }

        //Area
        if(contentRow.contains("дка") || contentRow.contains("кв.м")){
            if(contentRow.contains("кв.м")){
                pattern = Pattern.compile("(\\d+) кв\\.м");
                matcher = pattern.matcher(contentRow);
                if (matcher.find()){
                    estate.setArea(matcher.group(1).trim() + " кв.м");
                }
            }
            else if (contentRow.contains("дка")){
                pattern = Pattern.compile("(\\d+) дка ");
                matcher = pattern.matcher(contentRow);
                if (matcher.find()){
                    estate.setArea(matcher.group(1).trim() + " дка");
                }
            }
        }

        //PublishedBy
        estate.setListingUrl("olx.bg");

        return estate;
    }

    public List<Estate> handleCrawlerDataFromAloBg(String content){
        List<Estate> estateList = new ArrayList<>();

        content=content.replaceAll("Toggle navigation Публикувай (.+?) Степен на завършеност преди 30+ дни","");
        content=content.replaceAll("Сайт за обяви alo.bg (.+?) Вижте повече Ok","");
        //Every property to new line
        content=content.replaceAll("Вид на имота:\\s?","\n");
        content=content.replaceAll("Етажност:\\s?","\n");
        content=content.replaceAll("Вид квартира:\\s?","\n");
        content=content.replaceAll("/кв.м\\)\\s?","/кв.м)\n");
        content=content.replaceAll("от днес \\s?","\n");

        content=content.replaceAll("Квадратура: ","");
        content=content.replaceAll("Вид строителство: ","");
        content=content.replaceAll("Година на строителство: ","");

        //Separate data with comma for handling
        content=content.replaceAll(" (\\d+) кв\\.м",",$1 кв.м"); //sq.meters
        content=content.replaceAll("(\\d+ кв\\.м) (\\p{L}+)","$1,"); //property material
        content=content.replaceAll(" ([А-Яа-я\\s]+),\\s+(област\\s+[А-Яа-я\\s]+)",",$1,$2"); //region/address
        content=content.replaceAll(" (\\d+) етаж","$1,"); //Floors
        content=content.replaceAll("Обзавеждане:",",");
        content=content.replaceAll("Цена: ",","); //Price
        content=content.replaceAll("Номер на етажа:\\s?",","); //
        content=content.replaceAll(" (\\d{4}) г\\.","$1,"); //Year of construction
        content=content.replaceAll("(\\d{1,4} \\d{3}) EUR ","$1 EUR,");
        content=content.replaceAll("(\\d{1,4} \\d{3}) лв. ","$1 лв.,");

        // Split each content data into parts
        String[] rows = content.split("\n");

        for (String row : rows) {
            // Split each row into parts
            String[] parts = row.split(","); // ',' separates parameters
            if(parts.length > 1){
                if(!parts[0].contains("Toggle navigation")){
                    Estate estate = extractEstateForAloBg(parts);
                    if(estate.getTitle() != null){
                        estateList.add(estate);
                    }
                }
            }
        }
        return estateList;
    }

    private static Estate extractEstateForAloBg(String[] contentRow) {
        Estate estate = new Estate();

        estate.setTitle(contentRow[0]);

        //Area
        if(contentRow[1].contains("кв.м") && !contentRow[1].isBlank()){
            estate.setArea(contentRow[1]);
        }
        //Year of construction
        if(contentRow[2].trim().length() == 4 && !contentRow[2].isBlank()){
            estate.setYearOfConstruction(Integer.parseInt(contentRow[2].trim()));
        }

        //Floor
        if(contentRow[3].length()<3 && !contentRow[3].isBlank()){ // if true then [3] is floor
            estate.setFloor(Integer.parseInt(contentRow[3]));
        }
        else if(contentRow[4].length()<3 && !contentRow[4].isBlank()){
            estate.setFloor(Integer.parseInt(contentRow[4]));
        }

        //Price
        if(contentRow[contentRow.length - 2].contains("EUR") || contentRow[contentRow.length - 2].contains("лв.")){
            estate.setPrice(contentRow[contentRow.length - 2]);
        }

        //Location
        if(contentRow[contentRow.length - 3].contains("област Други държави") && contentRow[contentRow.length - 4].length() < 45) {
            //if true, set foreign country
            estate.setLocation(contentRow[contentRow.length - 4].trim());
        }
        else if(contentRow[contentRow.length - 4].length() < 15 && contentRow[contentRow.length - 3].contains("област")){
            // if length-4 is true then concat city and region
            estate.setLocation(contentRow[contentRow.length - 4] + ", " +  contentRow[contentRow.length - 3].trim());
        }
        else if(contentRow[contentRow.length - 3].contains("област")) {
            //if length-4 is false then have only region without city
            estate.setLocation(contentRow[contentRow.length - 3].trim());
        }
        else {
            estate.setLocation(null);
        }

        //PublishedBy
        estate.setListingUrl("alo.bg");

        return estate;
    }
}
