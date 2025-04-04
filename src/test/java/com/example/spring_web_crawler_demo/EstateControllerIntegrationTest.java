package com.example.spring_web_crawler_demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EstateControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void testGetAllEstatesTable() throws Exception {
        mockMvc.perform(get("/estates/get"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<table class=\"table align-middle\">")))
                .andExpect(content().string(containsString("<title>All Estate List</title>")))
                .andExpect(content().string(containsString("<button class=\"btn btn-outline-success\" type=\"submit\">Search</button>")))
                .andExpect(content().string(containsString("<button class=\"btn btn-outline-success\" type=\"submit\">All List</button>")))
                .andExpect(content().string(containsString("<a class=\"navbar-brand\" href=\"#\">")))
                .andExpect(content().string(containsString("<input class=\"form-control border-success focus:border-success me-2\" name=\"keyword\" type=\"search\" placeholder=\"Search\" aria-label=\"Search\">")))
                .andExpect(content().string(containsString("<li class=\"list-group-item\" style=\"width: 16rem;\">Estate Title</li>")));
    }

    @Test
    void testNotFoundPageWhenSearch() throws Exception {
        mockMvc.perform(get("/estates/search?keyword=notFound"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Not found Estate</title>")))
                .andExpect(content().string(containsString("<div class=\"d-flex align-items-top justify-content-center vh-100\">")))
                .andExpect(content().string(containsString("<button class=\"btn btn-outline-success\" type=\"submit\">Search</button>")))
                .andExpect(content().string(containsString("<button class=\"btn btn-outline-success\" type=\"submit\">All List</button>")))
                .andExpect(content().string(containsString("<h1 class=\"display-1 fw-bold\">No listings were found</h1>")))
                .andExpect(content().string(containsString("<p class=\"fs-3\"> <span class=\"text-danger\">Opps!</span> :/</p>")))
                .andExpect(content().string(containsString("Unfortunately, no listings were found with this keyword.")));
    }
}
