package com.example.spring_web_crawler_demo.services;
import com.example.spring_web_crawler_demo.entities.CrawledData;
import com.example.spring_web_crawler_demo.entities.Estate;
import com.example.spring_web_crawler_demo.repositories.EstateRepository;
import com.google.common.collect.ImmutableList;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MyCrawler extends WebCrawler{

    private final EstateRepository estateRepository;
    private final List<String> myCrawlDomains;

//    public MyCrawler(EstateRepository estateRepository){
//        this.estateRepository = estateRepository;
//    }

    public MyCrawler(List<String> myCrawlDomains, EstateRepository estateRepository) {
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

        String proxyHost = "127.0.0.1"; // Replace with a valid proxy IP
        int proxyPort = 9050; // Replace with a valid proxy port

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));

        try {
            Thread.sleep(3000);
            // Fetch and parse the page with Jsoup
            Document document = Jsoup.connect(url).get();
            String title = document.title();
            String content = document.body().text();

            // Create an entity object and save it to the database
            CrawledData data = new CrawledData();
            data.setUrl(url);
            data.setTitle(title);
            data.setContent(content);
            //repository.save(data); // Save to the database

            if (url.contains("imot") && !url.contains("q-къща/") && !url.contains("/ad/") && !url.contains("/ads/")){
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("debug-log.txt", true))) {
                    writer.write("Visited URL: " + url + "\n");
                    writer.write("title " + title + "\n");

                    if(url.contains("olx.bg")){
                        List<Estate> estatesOlx = handleCrawlerDataFromOlxBg(content);
                        if (!estatesOlx.isEmpty()){
                            estateRepository.saveAll(estatesOlx);
                            /*for (Estate estate : estatesOlx){
                                writer.write("content " + estate + "\n");
                            }*/
                        }
                    }else if (url.contains("alo.bg") && !content.contains("Публикувай обява Вход / Регистрация Вход")){
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

    private List<Estate> handleCrawlerDataFromOlxBg(String content){
        List<Estate> estateList = new ArrayList<>();
        content=content.replaceAll("Последвай \\s?",".\n");
        content=content.replaceAll("Промотирана обява\\s?","");
        content=content.replaceAll("Запази търсенето Ще те известим, когато има нови обяви по зададените критерии. Запази \\s?","");

        // Split each row into parts (adjust the delimiter as needed)
        String[] rows = content.split("\n");

        for (String row : rows) {
            // Split each row into parts (adjust the delimiter as needed)
            //String[] parts = row.split(","); // ',' separates parameters
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
        Pattern pattern = Pattern.compile("(.+?)\\s\\d{1,3}(?:\\s?\\d{3})*\\s(?:лв\\.|EUR)");
        Matcher matcher = pattern.matcher(contentRow);
        if(matcher.find()){
            estate.setTitle(matcher.group(1).trim());
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
            pattern = Pattern.compile("(?:лв\\.|€)\\s+(.*?)-");
        }
        matcher = pattern.matcher(contentRow);
        if (matcher.find()){
            estate.setLocation(matcher.group(1).trim());
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

    private List<Estate> handleCrawlerDataFromAloBg(String content){
        List<Estate> estateList = new ArrayList<>();
        //Every property to new line
        content=content.replaceAll("Вид на имота:\\s?","\n");
        content=content.replaceAll("Етажност:\\s?","\n");
        content=content.replaceAll("Вид квартира:\\s?","\n");
        content=content.replaceAll("/кв.м\\)\\s?","/кв.м)\n");
        content=content.replaceAll("от днес \\s?","\n");

        content=content.replaceAll("Квадратура: ","");
        content=content.replaceAll("Вид строителство: ","");

        //Separate data with comma for handling
        content=content.replaceAll("(\\s+\\d+) кв\\.м",",$1 кв.м"); //sq.meters
        content=content.replaceAll("(\\d+ кв\\.м) (\\p{L}+)","$1,$2"); //property material
        content=content.replaceAll(" ([А-Яа-я\\s]+),\\s+(област\\s+[А-Яа-я\\s]+)",",$1,$2"); //region/address
        content=content.replaceAll(" (\\d+) етаж",",$1,"); //Floors
        content=content.replaceAll("Обзавеждане:",",");
        content=content.replaceAll("Цена: ",","); //Price
        content=content.replaceAll("Номер на етажа:\\s?",","); //
        content=content.replaceAll("(\\d{4}) г\\.",",$1,"); //Year of construction
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

    public static Estate extractEstateForAloBg(String[] contentRow) {
        Estate estate = new Estate();

        estate.setTitle(contentRow[0]);
        estate.setArea(contentRow[1]);

        //Floor
        if(contentRow[3].length()<3){ // if true then [3] is floor
            estate.setFloor(Integer.parseInt(contentRow[3]));
        }
        else if (contentRow[3].length() == 4){ // if true then [3] is year
            estate.setYearOfConstruction(Integer.parseInt(contentRow[3]));
        }
        else { // if false then [3] is construction year, [4] is floor position
            estate.setYearOfConstruction(Integer.parseInt(contentRow[3]));
            estate.setFloor(Integer.parseInt(contentRow[4]));
        }

        //Price
        if(contentRow[contentRow.length - 2].contains("EUR") || contentRow[contentRow.length - 2].contains("лв.")){
            estate.setPrice(contentRow[contentRow.length - 2]);
        }

        //Location
        if(contentRow[contentRow.length - 3].contains("област Други държави") && contentRow[contentRow.length - 4].length() < 45) {
            //if true, set foreign country
            estate.setLocation(contentRow[contentRow.length - 4]);
        }
        else if(contentRow[contentRow.length - 4].length() < 15 && contentRow[contentRow.length - 3].contains("област")){
            // if length-4 is true then concat city and region
            estate.setLocation(contentRow[contentRow.length - 4] + ", " +  contentRow[contentRow.length - 3]);
        }
        else if(contentRow[contentRow.length - 3].contains("област")) {
            //if length-4 is false then have only region without city
            estate.setLocation(contentRow[contentRow.length - 3]);
        }
        else {
            estate.setLocation(null);
        }

        //PublishedBy
        estate.setListingUrl("alo.bg");

        return estate;
    }
}
