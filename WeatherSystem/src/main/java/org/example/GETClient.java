package org.example;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;

public class GETClient {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GETClient <server:port>");
            return;
        }

        String serverUrl = args[0];
        try {
            // Connect to the server
            Socket socket = new Socket(serverUrl.split(":")[0], Integer.parseInt(serverUrl.split(":")[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send GET request
            out.println("GET /weather.json HTTP/1.1");

            // Read the server's response
            String responseLine;
            StringBuilder response = new StringBuilder();
            while ((responseLine = in.readLine()) != null) {
                response.append(responseLine);
            }

            // Print the raw server response for debugging
            System.out.println("Server response: " + response.toString());

            // Now try parsing the response as JSON
            WeatherData weatherData = gson.fromJson(response.toString(), WeatherData.class);
            displayWeather(weatherData);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to display weather data
    private static void displayWeather(WeatherData data) {
        System.out.println("Weather for: " + data.name);
        System.out.println("Temperature: " + data.air_temp + "°C");
        System.out.println("Cloud: " + data.cloud);
        System.out.println("Wind Speed: " + data.wind_spd_kmh + " km/h");
    }
}
