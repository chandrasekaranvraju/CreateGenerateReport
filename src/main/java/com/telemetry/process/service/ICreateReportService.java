package com.telemetry.process.service;

public interface ICreateReportService {
    public String fetchTelemetryData();

    public <T> T transformRawData(String data);

    public <T> void writeToFile(T t, String fileName) ;
}
