package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Map;

public class GETClient {
    private static final Gson gson = new Gson();
    private static LamportClock lamportClock = new LamportClock();

        /**
     * Main entry point for the application
     * Connects to the server using command line arguments and sends a GET request,
     * then receives and processes the server's response.
     *
     * @param args Command line arguments, expected format is <server:port>
     */
    public static void main(String[] args) {
        // Check if command line arguments are provided
        if (args.length < 1) {
            System.out.println("Usage: java GETclient <server:port>");
            return;
        }

        // Get the server address and port number
        String serverUrl = args[0];

        try {
            // Connect to the server
            Socket socket = new Socket(serverUrl.split(":")[0], Integer.parseInt(serverUrl.split(":")[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Increment the Lamport clock to ensure correct ordering of events in a distributed system
            lamportClock.increment();

            // Send a GET request with the Lamport clock
            out.println("GET /weather.json HTTP/1.1");
            out.println("Lamport-Clock: " + lamportClock.getClock());
            out.println(); // Empty line to indicate end of headers

            // Read the server's response
            String responseLine;
            StringBuilder response = new StringBuilder();
            boolean isBody = false;

            while ((responseLine = in.readLine()) != null) {
                if (responseLine.isEmpty()) {
                    isBody = true;
                    continue;
                }
                if (isBody) {
                    response.append(responseLine);
                }
            }

            // Print the server's response
            System.out.println("Server response body: " + response.toString());

            // Process the response, removing the Lamport-Clock part
            String jsonResponse = response.toString().split("Lamport-Clock")[0].trim();

            // Use TypeToken to specify the type Map<String, WeatherData>
            Type weatherDataType = new TypeToken<Map<String, WeatherData>>() {}.getType();
            Map<String, WeatherData> weatherDataMap = gson.fromJson(jsonResponse, weatherDataType);
            displayWeather(weatherDataMap);

            // Close the connection
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the weather information stored in the given map.
     * Iterates through the map containing weather data, retrieving and displaying the weather information for each location.
     *
     * @param dataMap A map where the key is a string representing the location identifier, and the value is a WeatherData object containing the weather data.
     */
    private static void displayWeather(Map<String, WeatherData> dataMap) {
        // Iterates through the keys in the map, i.e., the location identifiers
        for (String id : dataMap.keySet()) {
            // Retrieves the WeatherData object corresponding to the current location identifier from the map
            WeatherData weather = dataMap.get(id);
            // Displays the name of the location
            System.out.println("Weather for: " + weather.name);
            // Displays the air temperature for the location
            System.out.println("Temperature: " + weather.air_temp + "°C");
            // Displays the cloud cover percentage for the location
            System.out.println("Cloud: " + weather.cloud);
            // Displays the wind speed for the location in km/h
            System.out.println("Wind Speed: " + weather.wind_spd_kmh + " km/h");
        }
    }
}
