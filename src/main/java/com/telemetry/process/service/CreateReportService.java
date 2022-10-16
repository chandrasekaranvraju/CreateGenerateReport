package com.telemetry.process.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.telemetry.process.exception.InvalidDataException;
import com.telemetry.process.exception.ProcessingException;
import com.telemetry.process.model.WeatherInfo;
import org.apache.commons.csv.CSVFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.csv.CSVPrinter;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CreateReportService implements ICreateReportService {

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
        logger.info("Process Telemetry Data");
        try {
            WeatherInfo weatherInfo = readXmlfromTelemetry();
            writeToFile(weatherInfo, fileName);
        } catch(Exception e){
            throw new ProcessingException("Exception occurred in process "+ e);
        }
        logger.info("Processing Telemetry Data Successful");
    }

    public WeatherInfo readXmlfromTelemetry() {
        String data = fetchTelemetryData();
        logger.info("Telemetry Data " + data);
        return transformRawData(data);
    }

    public WeatherInfo transformRawData(String data) {
        logger.info("Transform telemetry data");
        LocalDateTime timestamp = LocalDateTime.now();
        XmlMapper xmlMapper = new XmlMapper();
        WeatherInfo info = null;
        try {
            info = xmlMapper.readValue(data, WeatherInfo.class);
        } catch (JsonProcessingException e) {
            throw new InvalidDataException("Invalid Data/Invalid Format found "+e);
        }
        info.setTimestamp(timestamp.toString());
        logger.info("Transformed telemetry data successful");
        return info;
    }

    @Override
    public String fetchTelemetryData() {
        logger.info("Extract telemetry data");
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(telemetryUrl, String.class);
            if(response.getStatusCode() == HttpStatus.OK){
                return response.getBody();
            }else{
                throw new ProcessingException("Http Response Code received other than 200 "+response.getStatusCode());
            }
        } catch(HttpStatusCodeException e){
            return e.getResponseBodyAsString();
        } catch(RestClientResponseException e){
            return e.getResponseBodyAsString();
        }
    }

    @Override
    public <T> void writeToFile(T t, String fileName)  {
        logger.info("Write telemetry data to csv file");
        File csvOutputFile = new File(path+fileName);
        WeatherInfo weatherInfo = (WeatherInfo) t;

        logger.info("Writing file in path " + csvOutputFile.getAbsolutePath());
        boolean setHeader = csvOutputFile.exists();
        try(FileWriter writer = new FileWriter(csvOutputFile.getAbsolutePath(), true)){

            CSVPrinter csvPrinter = new CSVPrinter(writer,
                    CSVFormat.DEFAULT.builder().setSkipHeaderRecord(setHeader)
                            .setHeader("id", "temperature", "humidity", "location", "timestamp").build());
            logger.info("Writing file in csv format");
            csvPrinter.printRecord(weatherInfo.getId(), weatherInfo.getTemperature(), weatherInfo.getHumidity(), weatherInfo.getLocation(), weatherInfo.getTimestamp());
            csvPrinter.flush();
            csvPrinter.close();
        } catch (IOException e) {
            throw new ProcessingException("IOException occurred while writing to file "+ e);
        }
    }

}
