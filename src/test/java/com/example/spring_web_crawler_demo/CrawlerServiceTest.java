package com.example.spring_web_crawler_demo;

import com.example.spring_web_crawler_demo.entities.Estate;
import com.example.spring_web_crawler_demo.services.CrawlerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class CrawlerServiceTest {

    @Autowired
    CrawlerService crawlerService;

    @Test
    void testExtractEstateByOlxBgFromGivenStringContent() {
        //GIVEN
        String content = "Промотирана обява Тристаен в Несебър 113800 € гр. Несебър - Обновено на 23 март 2025 г. 87 кв.м - 1308.05 .";

        Estate estate = new Estate();
        estate.setTitle("Тристаен в Несебър");
        estate.setPrice("113800 €");
        estate.setLocation("гр. Несебър");
        estate.setArea("87 кв.м");
        estate.setListingUrl("olx.bg");
        List<Estate> estatesExpected = new ArrayList<>();
        estatesExpected.add(estate);

        //WHEN
        List<Estate> returnedEstates = crawlerService.handleCrawlerDataFromOlxBg(content);

        //THEN
        //add Override equals() in Estate to work
        Assertions.assertIterableEquals(estatesExpected, returnedEstates);
    }

    @Test
    void testExtractEstateByOlxBgFromGivenStringContentWhenParametersIsMissing(){
        //GIVEN
        String content = "Промотирана обява Тристаен в Несебър гр. Несебър - Обновено на 23 март 2025 г. 87 кв.м - 1308.05 .";

        Estate estate = new Estate();
        estate.setTitle("Тристаен в Несебър");
        //estate.setPrice("113800 €");
        estate.setLocation("гр. Несебър");
        estate.setArea("87 кв.м");
        estate.setListingUrl("olx.bg");
        List<Estate> estatesExpected = new ArrayList<>();
        estatesExpected.add(estate);

        //WHEN
        List<Estate> returnedEstates = crawlerService.handleCrawlerDataFromOlxBg(content);

        //THEN
        Assertions.assertIterableEquals(estatesExpected, returnedEstates);

    }

    @Test
    void testExtractEstateByAloBgFromGivenStringContent() {
        //GIVEN
        String content = "Вид на имота:Двустаен апартамент в Созопол Квадратура: 81 кв.м Вид строителство: Тухла Година на строителство: 2025 г. Номер на етажа: 3 етаж Етаж: Непоследен АКТ 14! На 116 метра от плажа! Двустаен Апартамент с площ 81.00кв.м, намиращ се на 3 ти жилищен етаж, недалеч от плажа. Разпределение; кухня с дневен тракт, спалня, баня с тоалетна. Имота е без такса поддръжка! Паркомясто от 6000 евро! Преимущества на сградата - прекрасна модерна сграда - без такса поддръжка - възможност за закупуване на паркомясто - разположена в живописната местност ”Каваците” - уютни апартаменти с функционални планировки Buzukovestates днешна обява Студио в Слънчев бряг. Ниска цена Камелия, Слънчев бряг, област Бургас Цена: 27 800 EUR (1 208.70 EUR/кв.м)";

        Estate estate = new Estate();
        estate.setTitle("Двустаен апартамент в Созопол");
        estate.setPrice("27 800 EUR");
        estate.setYearOfConstruction(2025);
        estate.setFloor(3);
        estate.setLocation("Слънчев бряг, област Бургас");
        estate.setArea("81 кв.м");
        estate.setListingUrl("alo.bg");

        List<Estate> estatesExpected = new ArrayList<>();
        estatesExpected.add(estate);

        //WHEN
        List<Estate> returnedEstates = crawlerService.handleCrawlerDataFromAloBg(content);

        //THEN
        Assertions.assertIterableEquals(estatesExpected, returnedEstates); //add Override equals() to work
    }

    @Test
    void testExtractEstateByAloBgFromGivenStringContentWhenParametersIsMissing(){
        //Missing Year and city, have only region

        //GIVEN
        String content = "Вид на имота:Двустаен апартамент в Созопол Квадратура: 81 кв.м Вид строителство: Тухла Номер на етажа: 3 етаж Етаж: Непоследен АКТ 14! На 116 метра от плажа! Двустаен Апартамент с площ 81.00кв.м, намиращ се на 3 ти жилищен етаж, недалеч от плажа. Разпределение; кухня с дневен тракт, спалня, баня с тоалетна. Имота е без такса поддръжка! Паркомясто от 6000 евро! Преимущества на сградата - прекрасна модерна сграда - без такса поддръжка - възможност за закупуване на паркомясто - разположена в живописната местност ”Каваците” - уютни апартаменти с функционални планировки Buzukovestates днешна обява Студио в Слънчев бряг. Ниска цена Камелия, област Бургас Цена: 27 800 EUR (1 208.70 EUR/кв.м)";

        Estate estate = new Estate();
        estate.setTitle("Двустаен апартамент в Созопол");
        estate.setPrice("27 800 EUR");
        //Year is missing in content
        estate.setFloor(3);
        estate.setLocation("област Бургас"); // Have only region in content
        estate.setArea("81 кв.м");
        estate.setListingUrl("alo.bg");

        List<Estate> estatesExpected = new ArrayList<>();
        estatesExpected.add(estate);

        //WHEN
        List<Estate> returnedEstates = crawlerService.handleCrawlerDataFromAloBg(content);

        //THEN
        Assertions.assertIterableEquals(estatesExpected, returnedEstates);
    }
}
