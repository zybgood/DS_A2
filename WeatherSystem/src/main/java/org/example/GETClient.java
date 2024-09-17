package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.net.*;
import java.lang.reflect.Type;
import java.util.Map;

public class GETClient {
    private static final Gson gson = new Gson();

        /**
     * Main entry point for the application.
     * This function connects to a specified server and retrieves weather data.
     *
     * @param args Command-line arguments, including the server address and port number
     */
    public static void main(String[] args) {
        // Check the number of command-line arguments
        if (args.length < 1) {
            System.out.println("Usage: java GETClient <server:port>");
            return;
        }

        // Get the server address and port number
        String serverUrl = args[0];

        try {
            // Connect to the server
            Socket socket = new Socket(serverUrl.split(":")[0], Integer.parseInt(serverUrl.split(":")[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send a GET request
            out.println("GET /weather.json HTTP/1.1");

            // Read the server's response
            String responseLine;
            StringBuilder response = new StringBuilder();
            boolean isBody = false;

            while ((responseLine = in.readLine()) != null) {
                if (responseLine.isEmpty()) {
                    // An empty line indicates the end of the HTTP headers
                    isBody = true;
                    continue;
                }
                if (isBody) {
                    // Read only the body part
                    response.append(responseLine);
                }
            }

            // Print the server's response body
            System.out.println("Server response body: " + response.toString());

            // Use Gson for JSON parsing
            Type weatherDataType = new TypeToken<Map<String, WeatherData>>() {}.getType();
            Map<String, WeatherData> weatherDataMap = gson.fromJson(response.toString(), weatherDataType);
            displayWeather(weatherDataMap);

            // Close the connection
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Displays the weather information stored in the data map.
     * Iterates through the map containing weather data, outputting each location's weather details.
     *
     * @param dataMap A map where the key is a string representing the location identifier, and the value is a WeatherData object containing the weather information.
     */
    private static void displayWeather(Map<String, WeatherData> dataMap) {
        // Iterates through the keys of the data map (location identifiers)
        for (String id : dataMap.keySet()) {
            // Retrieves the WeatherData object corresponding to the current location identifier
            WeatherData weather = dataMap.get(id);

            // Outputs the weather information for the current location
            System.out.println("Weather for: " + weather.name);
            System.out.println("Temperature: " + weather.air_temp + "°C");
            System.out.println("Cloud: " + weather.cloud);
            System.out.println("Wind Speed: " + weather.wind_spd_kmh + " km/h");
        }
    }
}
