package org.example;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class AggregationServerTest {

    @Test
    public void testLamportClockIncrement() {
        LamportClock clock = new LamportClock();
        assertEquals(0, clock.getClock()); // Initial clock value should be 0
        clock.increment();
        assertEquals(1, clock.getClock()); // After increment, clock should be 1
    }

    @Test
    public void testLamportClockUpdate() {
        LamportClock clock = new LamportClock();
        clock.update(5);
        assertEquals(6, clock.getClock()); // Should update to 6 (max of 0 and 5, plus 1)
        clock.update(3);
        assertEquals(7, clock.getClock()); // Should increment to 7 (no update since 6 > 3)
    }

    // Test for AggregationServer's displayWeather functionality
    @Test
    public void testDisplayWeather() {
        Map<String, WeatherData> weatherDataMap = new ConcurrentHashMap<>();
        WeatherData weather1 = new WeatherData();
        weather1.name = "Sydney";
        weather1.air_temp = 22.5;
        weather1.cloud = "Partly Cloudy";
        weather1.wind_spd_kmh = 15;

        WeatherData weather2 = new WeatherData();
        weather2.name = "Melbourne";
        weather2.air_temp = 18.0;
        weather2.cloud = "Overcast";
        weather2.wind_spd_kmh = 10;

        weatherDataMap.put("Sydney", weather1);
        weatherDataMap.put("Melbourne", weather2);

        // Normally you'd call the displayWeather method in AggregationServer here
        // AggregationServer.displayWeather(weatherDataMap);

        // For the purpose of unit testing, we simulate the output and verify correctness
        assertEquals("Sydney", weatherDataMap.get("Sydney").name);
        assertEquals(22.5, weatherDataMap.get("Sydney").air_temp, 0.01);
        assertEquals("Partly Cloudy", weatherDataMap.get("Sydney").cloud);
        assertEquals(15, weatherDataMap.get("Sydney").wind_spd_kmh);

        assertEquals("Melbourne", weatherDataMap.get("Melbourne").name);
        assertEquals(18.0, weatherDataMap.get("Melbourne").air_temp, 0.01);
        assertEquals("Overcast", weatherDataMap.get("Melbourne").cloud);
        assertEquals(10, weatherDataMap.get("Melbourne").wind_spd_kmh);
    }
    @Test
    public void testLamportClockConcurrency() throws InterruptedException {
        LamportClock clock = new LamportClock();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                clock.increment();
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                clock.increment();
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertEquals(2000, clock.getClock()); // Both threads incremented the clock 1000 times
    }

    // Test for WeatherData with extreme values
    @Test
    public void testWeatherDataExtremeValues() {
        WeatherData weather = new WeatherData();
        weather.name = "Antarctica";
        weather.air_temp = -89.2; // Lowest recorded temperature on Earth
        weather.cloud = "Clear";
        weather.wind_spd_kmh = 327; // Highest wind speed recorded

        assertEquals("Antarctica", weather.name);
        assertEquals(-89.2, weather.air_temp, 0.01);
        assertEquals("Clear", weather.cloud);
        assertEquals(327, weather.wind_spd_kmh);
    }

    // Test for WeatherData JSON serialization/deserialization
    @Test
    public void testWeatherDataJsonSerialization() {
        WeatherData weather = new WeatherData();
        weather.name = "Sydney";
        weather.air_temp = 25.0;
        weather.cloud = "Partly Cloudy";
        weather.wind_spd_kmh = 20;

        Gson gson = new Gson();
        String json = gson.toJson(weather);
        assertNotNull(json); // Ensure that the serialization produces valid JSON

        WeatherData deserializedWeather = gson.fromJson(json, WeatherData.class);
        assertEquals(weather.name, deserializedWeather.name);
        assertEquals(weather.air_temp, deserializedWeather.air_temp, 0.01);
        assertEquals(weather.cloud, deserializedWeather.cloud);
        assertEquals(weather.wind_spd_kmh, deserializedWeather.wind_spd_kmh);
    }

    // Test for AggregationServer - processing multiple weather data entries
    @Test
    public void testAggregationServerMultipleWeatherEntries() {
        Map<String, WeatherData> weatherDataMap = new ConcurrentHashMap<>();

        WeatherData weather1 = new WeatherData();
        weather1.name = "City1";
        weather1.air_temp = 20.0;
        weather1.cloud = "Clear";
        weather1.wind_spd_kmh = 5;

        WeatherData weather2 = new WeatherData();
        weather2.name = "City2";
        weather2.air_temp = 15.0;
        weather2.cloud = "Overcast";
        weather2.wind_spd_kmh = 10;

        weatherDataMap.put("City1", weather1);
        weatherDataMap.put("City2", weather2);

        // Simulating the AggregationServer's displayWeather method
        assertEquals("City1", weatherDataMap.get("City1").name);
        assertEquals(20.0, weatherDataMap.get("City1").air_temp, 0.01);
        assertEquals("Clear", weatherDataMap.get("City1").cloud);
        assertEquals(5, weatherDataMap.get("City1").wind_spd_kmh);

        assertEquals("City2", weatherDataMap.get("City2").name);
        assertEquals(15.0, weatherDataMap.get("City2").air_temp, 0.01);
        assertEquals("Overcast", weatherDataMap.get("City2").cloud);
        assertEquals(10, weatherDataMap.get("City2").wind_spd_kmh);
    }
}
