package com.telemetry.process.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherInfo {
    private int id;
    private int temperature;
    private int humidity;
    private String location;
    private String timestamp;
}
