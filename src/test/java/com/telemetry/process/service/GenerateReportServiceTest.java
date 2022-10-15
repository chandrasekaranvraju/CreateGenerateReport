package com.telemetry.process.service;

import com.telemetry.process.model.WeatherInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GenerateReportServiceTest {


    GenerateReportService generateReportService = new GenerateReportService();

    @Test
    void listFilesForAggregation_test() {
        WeatherInfo info = new WeatherInfo(105,116,83,"35.56,45.98","2022-10-15T00:06:00.205958713");
        WeatherInfo info2 = new WeatherInfo(105,116,83,"35.56,45.98","2022-10-15T00:06:00.205958713");
        WeatherInfo info3 = new WeatherInfo(105,116,83,"35.56,45.98","2022-10-15T00:06:00.205958713");
        WeatherInfo info4 = new WeatherInfo(105,116,83,"35.56,45.98","2022-10-15T00:06:00.205958713");

        generateReportService.writeToCSVFile(Arrays.asList(info,info2,info3,info4),
                "src/test/resources/","testFileAggregate.csv");
        List<File> files = generateReportService.listFilesForAggregation("src/test/resources/","testFileAggregate");
        assertEquals(files.get(0).getAbsolutePath(),new File("src/test/resources/testFileAggregate.csv").getAbsolutePath());

    }

    @Test
    void readDataFromFile_test(){
        List<File> files = Arrays.asList(
            new File("src/test/resources/test1.csv"),
            new File("src/test/resources/test2.csv"),
            new File("src/test/resources/test3.csv"));
        List<WeatherInfo> weatherInfo = generateReportService.readDataFromFile(files);
        assertEquals(13, weatherInfo.size());
    }
    @Test
    void writeToCSVFile_test() {
        WeatherInfo info = new WeatherInfo(105,116,83,"35.56,45.98","2022-10-15T00:06:00.205958713");
        WeatherInfo info2 = new WeatherInfo(105,116,83,"35.56,45.98","2022-10-15T00:06:00.205958713");
        WeatherInfo info3 = new WeatherInfo(105,116,83,"35.56,45.98","2022-10-15T00:06:00.205958713");
        WeatherInfo info4 = new WeatherInfo(105,116,83,"35.56,45.98","2022-10-15T00:06:00.205958713");

        String reportPath = "src/test/resources/";
        String reportName = "testReport.csv";
        generateReportService.writeToCSVFile(Arrays.asList(info,info2,info3,info4), reportPath,reportName);
        assertTrue(new File(reportPath+reportName).exists());
    }

    @Test
    void mapToWeatherInfo_test() throws IOException {
        Reader in = new FileReader(new File("src/test/resources/test1.csv"));
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
        for(CSVRecord record: records) {
            WeatherInfo info = generateReportService.mapToWeatherInfo(record);
            assertEquals(record.get(0), String.valueOf(info.getId()));
            assertEquals(record.get(1), String.valueOf(info.getTemperature()));
            assertEquals(record.get(2), String.valueOf(info.getHumidity()));
            assertEquals(record.get(3), info.getLocation());

        }
        }

    @BeforeTestClass
    void setup(){

    }
}