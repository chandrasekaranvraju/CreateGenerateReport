package com.telemetry.process.service;

import com.telemetry.process.model.WeatherInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.file.attribute.BasicFileAttributes;


import java.io.*;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Component
public class GenerateReportService {

    @Value("${generateReport.reportpath}")
    private String reportPath;

    @Value("${generateReport.readpath}")
    private String dataPath;

    @Value("${generateReport.filename}")
    private String reportFileName;

    private static final Logger logger = LoggerFactory.getLogger(GenerateReportService.class);

    public void generate() throws IOException {

        logger.info("Generate Report Scheduled");
        List<WeatherInfo> info = new ArrayList<>();
        List<File> reportFile = new ArrayList<>();
                File[] files = new File(dataPath).listFiles(file -> file.isFile()
                        && file.getName().startsWith("WeatherRawData") && file.getName().endsWith(".csv"));
                logger.info("Number of raw data files"+files.length);
                  for(File f:files)
                    {
                    BasicFileAttributes fileAttributes = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                     if(System.currentTimeMillis() - fileAttributes.creationTime().to(TimeUnit.MILLISECONDS) <= 10 * 60 * 1000)
                        reportFile.add(f);
                    }
                  logger.info("Number of files matching criteria "+reportFile.size());
        for(File f: reportFile){
            Reader in = new FileReader(f);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .builder().setSkipHeaderRecord(true).build()
                    .parse(in);
            info.addAll(StreamSupport.stream(records.spliterator(), false)
                    .filter(s -> {
                        logger.info("Temperature "+s.get(1));
                        return (Integer.parseInt(s.get(1)) >= 45);
                    })
                    .map(r -> mapToWeatherInfo(r))
                    .toList());
            }
        logger.info("Number of weatherdata matching criteria "+info.size());

        writeToCSVFile(info);



    }

    public void writeToCSVFile(List<WeatherInfo> info) throws IOException {
        logger.info("Write to CSV File");

        String fileName = new StringBuilder()
                .append(reportPath)
                .append(MessageFormat.format(reportFileName, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))))
                .toString();
        File reportFile = new File(fileName);
        FileWriter writer = new FileWriter(reportFile.getAbsolutePath(),true);
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .builder().setSkipHeaderRecord(false)
                .setHeader("id", "temperature", "humidity","location","timestamp").build());
        for(WeatherInfo weather: info) {
            logger.info("Writing to CSV File"+weather);

            csvPrinter.printRecord(weather.getId(), weather.getTemperature(), weather.getHumidity(), weather.getLocation(), weather.getTimestamp());
        }
        csvPrinter.flush();
    }

    private WeatherInfo mapToWeatherInfo(CSVRecord csvRecord) {
        return  new WeatherInfo(Integer.parseInt(csvRecord.get(0)),
                Integer.parseInt(csvRecord.get(1)),
                Integer.parseInt(csvRecord.get(2)),csvRecord.get(3),csvRecord.get(4));
    }
}
