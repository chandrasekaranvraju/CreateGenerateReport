package com.telemetry.process.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.telemetry.process.model.WeatherInfo;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CreateReportServiceTest {

    private String telemetryUrl;

    private String path;

    @InjectMocks
    private CreateReportService reportService;

    @Mock
    private RestTemplate restTemplate;



    @BeforeEach
    void setUp() {
    }

    @Test
    void writeToFile() throws IOException {
        WeatherInfo expected = new WeatherInfo(23,75,75,"35.56.45.98", LocalDateTime.now().toString());
        String filename = "createReport.csv";
        reportService.writeToFile(expected,filename);
        assertTrue(new File(path+filename).exists());
    }

    @Test
    void transformRawData() throws JsonProcessingException {
        String data = "<data><id>23</id><temperature>75</temperature><humidity>75</humidity><location>35.56,45.98</location></data>";
        WeatherInfo info = reportService.transformRawData(data);
        WeatherInfo expected = new WeatherInfo(23,75,75,"35.56.45.98", LocalDateTime.now().toString());
        assertEquals(expected.getId(),info.getId());
    }

    @Test
    void fetchWeatherData_test() {
        String response = "<data><id>0105</id><temperature>75</temperature><humidity>75</humidity><location>35.56,45.98</location></data>";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(response,HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(telemetryUrl, String.class)).thenReturn(responseEntity);
        String telemetryResponse = reportService.fetchWeatherData();
        assertEquals(response, telemetryResponse);
    }
}