package com.smarttravel.tripplanner.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("trips")
public class TripEntity {

    @Id
    private Long id;

    private String city;
    private String country;
    private String countryCode;

    private double latitude;
    private double longitude;
    private String timezone;

    private String fromCurrency;
    private String toCurrency;

    private BigDecimal exchangeRate;
    private BigDecimal budget;
    private BigDecimal convertedBudget;

    private double temperatureC;
    private double apparentTemperatureC;
    private double windSpeedKmh;
    private int weatherCode;
    private String weatherCondition;

    private Instant generatedAt;
    private Instant createdAt;

    public TripEntity() {
    }

    public TripEntity(
            Long id,
            String city,
            String country,
            String countryCode,
            double latitude,
            double longitude,
            String timezone,
            String fromCurrency,
            String toCurrency,
            BigDecimal exchangeRate,
            BigDecimal budget,
            BigDecimal convertedBudget,
            double temperatureC,
            double apparentTemperatureC,
            double windSpeedKmh,
            int weatherCode,
            String weatherCondition,
            Instant generatedAt,
            Instant createdAt) {

        this.id = id;
        this.city = city;
        this.country = country;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.exchangeRate = exchangeRate;
        this.budget = budget;
        this.convertedBudget = convertedBudget;
        this.temperatureC = temperatureC;
        this.apparentTemperatureC = apparentTemperatureC;
        this.windSpeedKmh = windSpeedKmh;
        this.weatherCode = weatherCode;
        this.weatherCondition = weatherCondition;
        this.generatedAt = generatedAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getFromCurrency() {
        return fromCurrency;
    }

    public String getToCurrency() {
        return toCurrency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public BigDecimal getConvertedBudget() {
        return convertedBudget;
    }

    public double getTemperatureC() {
        return temperatureC;
    }

    public double getApparentTemperatureC() {
        return apparentTemperatureC;
    }

    public double getWindSpeedKmh() {
        return windSpeedKmh;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}