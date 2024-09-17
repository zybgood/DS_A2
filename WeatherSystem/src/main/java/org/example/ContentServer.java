package org.example;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;

public class ContentServer {
    private static final Gson gson = new Gson();
    private static LamportClock lamportClock = new LamportClock();

    /**
     * The main entry point of the program.
     * Reads weather data from a file, converts it to JSON format, and sends it to the specified server.
     *
     * @param args Command line arguments, format: <server:port> <file_path>
     */
    public static void main(String[] args) {
        // Check if the number of command line arguments is sufficient
        if (args.length < 2) {
            System.out.println("Usage: java ContentServer <server:port> <file_path>");
            return;
        }

        // Extract server address and file path from command line arguments
        String serverUrl = args[0];
        String filePath = args[1];

        try {
            // Read weather data from the file
            WeatherData data = readFromFile(filePath);
            // Check if the read data is valid
            if (data == null) {
                System.out.println("Invalid file data");
                return;
            }

            // Convert WeatherData to JSON
            String jsonData = gson.toJson(data);
            // Output the JSON data to be sent
            System.out.println("Sending JSON: " + jsonData);

            // Connect to the server
            Socket socket = new Socket(serverUrl.split(":")[0], Integer.parseInt(serverUrl.split(":")[1]));
            // Create a writer for sending data
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Increment Lamport clock before sending request
            lamportClock.increment();

            // Prepare and send HTTP PUT request
            out.println("PUT /weather.json HTTP/1.1");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + jsonData.length());
            out.println("Lamport-Clock: " + lamportClock.getClock());
            out.println();
            out.println(jsonData);

            // Close the connection
            socket.close();
        } catch (Exception e) {
            // Print exception information in case of error
            e.printStackTrace();
        }
    }


    /**
     * Reads weather data from a file and returns a WeatherData object.
     * @param filePath The path to the file containing weather data.
     * @return A WeatherData object populated with the data from the file.
     * @throws IOException If there is an issue reading or processing the file.
     */
    private static WeatherData readFromFile(String filePath) throws IOException {
        // Create a BufferedReader to read the file
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        // Create a WeatherData object to store the read data
        WeatherData data = new WeatherData();

        // Read each line of the file
        String line;
        while ((line = reader.readLine()) != null) {
            // Split each line into parts
            String[] parts = line.split(":");

            // Ensure there are at least two parts after splitting
            if (parts.length < 2) continue;

            // Process the data based on its type
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

        // Close the BufferedReader
        reader.close();

        // Return the WeatherData object filled with data
        return data;
    }

}
