package org.example;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;

public class ContentServer {
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <server:port> <file_path>");
            return;
        }

        String serverUrl = args[0];

        // Parse server name and port
        String[] serverInfo = args[0].split(":");
        String serverName = serverInfo[0];
        int port = Integer.parseInt(serverInfo[1]);  // Make sure port is correctly parsed as an integer

        String filePath = args[1];

        System.out.println("Connecting to server " + serverName + " on port " + port);
        System.out.println("Reading data from file: " + filePath);

        try {
            // Read weather data from file and create WeatherData object
            WeatherData data = readFromFile(filePath);
            if (data == null) {
                System.out.println("Invalid file data");
                return;
            }

            // Convert WeatherData object to JSON
            String jsonData = gson.toJson(data);
            System.out.println("Sending JSON: " + jsonData);  // Debugging

            // Send PUT request
            Socket socket = new Socket(serverUrl.split(":")[0], Integer.parseInt(serverUrl.split(":")[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send HTTP headers and JSON body
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + jsonData.length());
            out.println();
            out.println(jsonData);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to read weather data from file and construct WeatherData object
    private static WeatherData readFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        WeatherData data = new WeatherData();

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length < 2) continue; // Skip invalid lines

            switch (parts[0].trim()) {
                case "id":
                    data.id = parts[1].trim();
                    break;
                case "name":
                    data.name = parts[1].trim();
                    break;
                case "state":
                    data.state = parts[1].trim();
                    break;
                case "time_zone":
                    data.time_zone = parts[1].trim();
                    break;
                case "lat":
                    data.lat = Double.parseDouble(parts[1].trim());
                    break;
                case "lon":
                    data.lon = Double.parseDouble(parts[1].trim());
                    break;
                case "local_date_time":
                    data.local_date_time = parts[1].trim();
                    break;
                case "local_date_time_full":
                    data.local_date_time_full = parts[1].trim();
                    break;
                case "air_temp":
                    data.air_temp = Double.parseDouble(parts[1].trim());
                    break;
                case "apparent_t":
                    data.apparent_t = Double.parseDouble(parts[1].trim());
                    break;
                case "cloud":
                    data.cloud = parts[1].trim();
                    break;
                case "dewpt":
                    data.dewpt = Double.parseDouble(parts[1].trim());
                    break;
                case "press":
                    data.press = Double.parseDouble(parts[1].trim());
                    break;
                case "rel_hum":
                    data.rel_hum = Integer.parseInt(parts[1].trim());
                    break;
                case "wind_dir":
                    data.wind_dir = parts[1].trim();
                    break;
                case "wind_spd_kmh":
                    data.wind_spd_kmh = Integer.parseInt(parts[1].trim());
                    break;
                case "wind_spd_kt":
                    data.wind_spd_kt = Integer.parseInt(parts[1].trim());
                    break;
            }
        }
        reader.close();
        return data;
    }
}
