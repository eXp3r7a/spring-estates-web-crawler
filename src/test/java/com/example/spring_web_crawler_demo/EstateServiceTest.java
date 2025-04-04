package com.example.spring_web_crawler_demo;

import com.example.spring_web_crawler_demo.controllers.EstateController;
import com.example.spring_web_crawler_demo.entities.Estate;
import com.example.spring_web_crawler_demo.repositories.EstateRepository;
import com.example.spring_web_crawler_demo.services.EstateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EstateServiceTest {

    @InjectMocks
    EstateService estateService;

    @Mock
    EstateRepository estateRepository;

    @InjectMocks
    EstateController estateController;

    @Mock
    Model model;

    @Test
    void testEstateRepositoryQueryWhenFound(){
        //GIVEN
        Estate estate = new Estate();
        estate.setTitle("Тристаен в Несебър");
        estate.setPrice("113800 €");
        estate.setLocation("гр. Несебър");
        estate.setArea("87 кв.м");
        estate.setListingUrl("olx.bg");
        List<Estate> estatesExpected = new ArrayList<>();
        estatesExpected.add(estate);

        when(estateRepository.searchEstates("Несебър")).thenReturn(estatesExpected);

        //WHEN
        List<Estate> returnedEstates = estateService.searchEstates("Несебър");

        //THEN
        Assertions.assertEquals(1,returnedEstates.size());
        Assertions.assertEquals(estatesExpected,returnedEstates);
        verify(estateRepository,times(1)).searchEstates("Несебър");
    }

    @Test
    void testEstateRepositoryQueryWhenNotFound(){
        //GIVEN
        List<Estate> estatesExpected = new ArrayList<>();
        String expected = "table/not_found";
        //Listing is empty
        when(estateRepository.searchEstates("Несебър")).thenReturn(estatesExpected);

        //WHEN
        String returned = estateController.searchEstates("Несебър", model);

        //THEN
        Assertions.assertEquals(expected,returned);
        verify(estateRepository,times(1)).searchEstates("Несебър");
    }
}
