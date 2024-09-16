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
            // Read weather data from file
            WeatherData data = readFromFile(filePath);
            if (data == null) {
                System.out.println("Invalid file data");
                return;
            }

            // Send PUT request
            Socket socket = new Socket(serverUrl.split(":")[0], Integer.parseInt(serverUrl.split(":")[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("PUT /weather.json HTTP/1.1");
            out.println("Content-Type: application/json");
            out.println("Content-Length: " + gson.toJson(data).length());
            out.println();
            out.println(gson.toJson(data));

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static WeatherData readFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        WeatherData data = new WeatherData();

        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts[0].equals("id")) data.id = parts[1];
            // Populate other fields here
        }
        reader.close();
        return data;
    }
}

