package org.example;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;

public class ContentServer {
    private static final Gson gson = new Gson();

    /**
     * The main entry point of the program.
     * Sends weather data from a local file to a specified server using a PUT request.
     *
     * @param args Command line arguments, requiring at least two parameters:
     *             0: Server address and port (in the form server:port)
     *             1: Local file path containing weather data
     */
    public static void main(String[] args) {
        // Check if the number of command line arguments is sufficient
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <server:port> <file_path>");
            return;
        }

        // Store the server URL for later use
        String serverUrl = args[0];

        // Parse server name and port
        String[] serverInfo = args[0].split(":");
        String serverName = serverInfo[0];
        int port = Integer.parseInt(serverInfo[1]);  // Ensure port is correctly parsed as an integer

        // Store the file path for later use
        String filePath = args[1];

        // Print the server information and file path to be processed
        System.out.println("Connecting to server " + serverName + " on port " + port);
        System.out.println("Reading data from file: " + filePath);

        try {
            // Read weather data from file and create WeatherData object
            WeatherData data = readFromFile(filePath);
            // Check if the read data is valid
            if (data == null) {
                System.out.println("Invalid file data");
                return;
            }

            // Convert WeatherData object to JSON format
            String jsonData = gson.toJson(data);
            // Print the JSON data to be sent for debugging purposes
            System.out.println("Sending JSON: " + jsonData);

            // Establish a connection to the server
            Socket socket = new Socket(serverUrl.split(":")[0], Integer.parseInt(serverUrl.split(":")[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send HTTP PUT request with necessary headers
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + jsonData.length());
            out.println();
            // Send the JSON formatted request body
            out.println(jsonData);

            // Close the socket connection
            socket.close();
        } catch (Exception e) {
            // Print exception information for any exceptions
            e.printStackTrace();
        }
    }


        /**
     * Reads weather data from the specified file path.
     *
     * @param filePath The file path pointing to a file containing weather data.
     * @return Returns a WeatherData object filled with the weather data read from the file.
     * @throws IOException If an I/O error occurs while reading or closing the file.
     */
    private static WeatherData readFromFile(String filePath) throws IOException {
        // Create a BufferedReader to read the file
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        // Create a WeatherData object to store the read weather data
        WeatherData data = new WeatherData();

        // Read the file content line by line
        String line;
        while ((line = reader.readLine()) != null) {
            // Split each line using ":" as the delimiter
            String[] parts = line.split(":");
            // If there are fewer than 2 parts, skip the line
            if (parts.length < 2) continue;

            // Assign values to the corresponding properties of the WeatherData object based on the data type keywords
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
        // Close the file reader
        reader.close();
        // Return the WeatherData object filled with weather data
        return data;
    }

}
