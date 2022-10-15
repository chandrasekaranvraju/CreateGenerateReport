package com.telemetry.process.service;

import com.telemetry.process.model.WeatherInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface IGenerateReportService {

    void generate();

    public <T> void writeToCSVFile(T t, String reportPath, String reportFileName) ;

}
