package com.telemetry.process.service;

import com.telemetry.process.exception.ProcessingException;
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
public class GenerateReportService implements IGenerateReportService{

    @Value("${generateReport.reportpath}")
    private String reportPath;

    @Value("${generateReport.readpath}")
    private String dataPath;

    @Value("${generateReport.filename}")
    private String reportFileName;

    @Value("${generateReport.prefix}")
    private String filePrefix;

    private static final Logger logger = LoggerFactory.getLogger(GenerateReportService.class);

    public void generate()  {
        logger.info("Generate Report Scheduled");
        try{
            List<WeatherInfo> info = new ArrayList<>();
            List<File> reportFile = listFilesForAggregation();
            logger.info("Number of files available for aggregation in the specified duration "+reportFile.size());
            info = readDataFromFile(reportFile);
            logger.info("Data matching criteria (temperature > 45)"+ " No of files matched " + info.size());
            writeToCSVFile(info);
        } catch(Exception e){
            throw new ProcessingException("Exception occurred in generate "+ e);
        }
    }

    public List<File> listFilesForAggregation(){
        List<File> reportFile = new ArrayList<>();
        try {
            File[] files = new File(dataPath).listFiles(file -> file.isFile()
                    && file.getName().startsWith(filePrefix) && file.getName().endsWith(".csv"));
            logger.info("Number of raw data files " + files.length);
            for (File f : files) {
                BasicFileAttributes
                    fileAttributes = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                if (System.currentTimeMillis() - fileAttributes.lastAccessTime().to(TimeUnit.MILLISECONDS) <= 10 * 60 * 1000)
                    reportFile.add(f);
            }
        }catch(IOException e){
            throw new ProcessingException("IOException Occurred while listing the files for aggregation "+e);
        }
        return reportFile;
    }

    public List<WeatherInfo> readDataFromFile(List<File> reportFile) {
        List<WeatherInfo> info = new ArrayList<>();
        for(File f: reportFile){
            try(Reader in = new FileReader(f)){
                Iterable<CSVRecord> records = CSVFormat.DEFAULT
                            .builder().setSkipHeaderRecord(true).build()
                            .parse(in);

                info.addAll(StreamSupport.stream(records.spliterator(), false)
                    .filter(s -> Integer.parseInt(s.get(1)) >= 45)
                    .map(r -> mapToWeatherInfo(r))
                    .toList());
            } catch (IOException e) {
                throw new ProcessingException("IOException Occurred while reading data from file "+e);
            }
        }
        return info;
    }

    public <T> void writeToCSVFile(T t)  {
        List<WeatherInfo> info = (List<WeatherInfo>) t;
        String fileName = new StringBuilder()
                .append(reportPath)
                .append(MessageFormat.format(reportFileName, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))))
                .toString();
        logger.info("Write to CSV File "+fileName);
        File reportFile = new File(fileName);
        try(FileWriter writer = new FileWriter(reportFile.getAbsolutePath(),true)){
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .builder().setSkipHeaderRecord(false)
                        .setHeader("id", "temperature", "humidity","location","timestamp").build());

            for(WeatherInfo weather: info) {
                logger.debug("Writing to CSV File" + weather);
                csvPrinter.printRecord(weather.getId(), weather.getTemperature(), weather.getHumidity(), weather.getLocation(), weather.getTimestamp());
            }
            csvPrinter.flush();
            csvPrinter.close();
        } catch (IOException e) {
            throw new ProcessingException("IOException Occurred while writing to CSV file "+ e);
        }
    }

    private WeatherInfo mapToWeatherInfo(CSVRecord csvRecord) {
        return  new WeatherInfo(Integer.parseInt(csvRecord.get(0)),
                Integer.parseInt(csvRecord.get(1)),
                Integer.parseInt(csvRecord.get(2)),csvRecord.get(3),csvRecord.get(4));
    }
}
