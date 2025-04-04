package com.example.spring_web_crawler_demo;

import com.example.spring_web_crawler_demo.services.CrawlerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CrawlerControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Mock
    CrawlerService crawlerService;

    @Test
    void testStartCrawlerWhenSuccessful() throws Exception {
        mockMvc.perform(get("/start-crawl"))
                .andExpect(status().isOk())
                .andExpect(content().string("Crawling has started!"));
    }

    @Test
    void testStartCrawlerWhenFail() throws Exception {
        //GIVEN
        String expectThrowMessage = "Failed to start crawler: ";

        //WHEN
        when(crawlerService.startCrawler()).thenThrow(new RuntimeException("Failed to start crawler: "));

        //THEN
        Assertions.assertEquals(expectThrowMessage, "Failed to start crawler: ");
    }

}
