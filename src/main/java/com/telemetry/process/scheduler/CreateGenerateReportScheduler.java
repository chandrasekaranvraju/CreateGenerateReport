package com.telemetry.process.scheduler;

import com.telemetry.process.service.CreateReportService;
import com.telemetry.process.service.GenerateReportService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
public class CreateGenerateReportScheduler {

    @Autowired
    CreateReportService createReportService;

    @Autowired
    GenerateReportService generateReportService;
    private static final Logger logger = LoggerFactory.getLogger(CreateGenerateReportScheduler.class);

    @Value("${createReport.filename}")
    private String outputFileName;

    @Value("${createReport.path}")
    private String outputPath;
    private String fileName;

    @Scheduled(cron = "${createReport.cron.expression}")
    public void createReport() throws IOException {
        createReportService.process(generateFileName());
    }

    public String generateFileName() throws IOException {
        if(fileName != null){
            logger.info("check filePath "+outputPath+fileName);
            BasicFileAttributes fileAttributes = Files.readAttributes(new File(outputPath+fileName).toPath(), BasicFileAttributes.class);
            logger.info("time "+fileAttributes.lastAccessTime().to(TimeUnit.MILLISECONDS)+" "+System.currentTimeMillis());
            logger.info("time difference" + (System.currentTimeMillis() - fileAttributes.lastAccessTime().to(TimeUnit.MILLISECONDS)));

            if ((System.currentTimeMillis() - fileAttributes.lastAccessTime().to(TimeUnit.MILLISECONDS)) >= 5 * 60 * 1000) {
                logger.info("New file to be created");
                String fileTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
                fileName = MessageFormat.format(outputFileName, fileTime);
                logger.info("new filePath "+fileName);
            }
        }else{
            String fileTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            fileName = MessageFormat.format(outputFileName, fileTime);
            logger.info("first filePath "+fileName);
        }
        return fileName;
    }

    @Scheduled(cron = "${generateReport.cron.expression}")
    public void generateReport() throws IOException {
        generateReportService.generate();
    }
}
