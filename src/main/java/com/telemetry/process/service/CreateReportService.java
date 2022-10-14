package com.telemetry.process.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.telemetry.process.exception.InvalidDataException;
import com.telemetry.process.model.WeatherInfo;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.csv.CSVPrinter;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class CreateReportService {

    @Value("${telemetry.url}")
    private String telemetryUrl;

    @Value("${createReport.filename}")
    private String outputFileName;

    @Value("${createReport.path}")
    private String path;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(CreateReportService.class);

    public void process(String fileName) {
        try {
            WeatherInfo weatherInfo = readXmlfromTelemetry();
            writeToFile(weatherInfo, fileName);
        } catch (JsonProcessingException e) {
            throw new InvalidDataException(e);
        } catch (IOException e) {
            throw new InvalidDataException(e);
        }

    }

    public void writeToFile(WeatherInfo weatherInfo, String fileName) throws IOException {
       File csvOutputFile = new File(path+fileName);

        logger.info("fileName "+csvOutputFile.getAbsolutePath()+" "+csvOutputFile.getName());
            FileWriter writer = new FileWriter(csvOutputFile.getAbsolutePath(),true);
            CSVPrinter csvPrinter = new CSVPrinter(writer,
                    CSVFormat.DEFAULT.builder().setSkipHeaderRecord(csvOutputFile.exists())
                            .setHeader("id", "temperature", "humidity","location","timestamp").build());

        csvPrinter.printRecord(weatherInfo.getId(),weatherInfo.getTemperature(),weatherInfo.getHumidity(),weatherInfo.getLocation(),weatherInfo.getTimestamp());
        csvPrinter.flush();
    }


    public WeatherInfo readXmlfromTelemetry() throws JsonProcessingException {
        String data = fetchWeatherData();
        logger.info("Telemetry Data " + data);
        return transformRawData(data);
    }

    public String fetchWeatherData(){
        ResponseEntity<String> response = restTemplate.getForEntity(telemetryUrl, String.class);
        return response.getBody();
    }

    public WeatherInfo transformRawData(String data) throws JsonProcessingException {
        LocalDateTime timestamp = LocalDateTime.now();
        XmlMapper xmlMapper = new XmlMapper();
        WeatherInfo info = xmlMapper.readValue(data, WeatherInfo.class);
        info.setTimestamp(timestamp.toString());
        return info;
    }
}
