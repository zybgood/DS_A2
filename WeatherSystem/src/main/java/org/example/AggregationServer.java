package org.example;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AggregationServer {
        // A ConcurrentHashMap to store weather data with string keys and WeatherData values
    private static Map<String, WeatherData> weatherDataMap = new ConcurrentHashMap<>();

    // The port number the server listens on
    private static int port = 4567;

    // Timestamp of the last weather data update
    private static long lastUpdateTime = System.currentTimeMillis();

    // Timeout duration set to 30 seconds
    private static final int TIMEOUT = 30000;

    // Gson instance for JSON parsing and serialization
    private static final Gson gson = new Gson();


    /**
     * The main entry point of the program.
     * This method starts a server, listens on a specified port, and handles client connections.
     *
     * @param args Command-line arguments, which can be used to specify the server port
     */
    public static void main(String[] args) {
        // Check command-line arguments and set the server port if provided
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        try (
            // Create a server socket and start listening on the specified port
            ServerSocket serverSocket = new ServerSocket(port)
        ) {
            // Output server startup information
            System.out.println("Server is running on port " + port);
            // Continuously listen and handle client connections
            while (true) {
                // Accept a client connection and create a new client handler thread
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            // Handle potential IO exceptions
            e.printStackTrace();
        }
    }



        static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;

        /**
         * Constructs a new ClientHandler with the specified client socket.
         *
         * @param socket The client socket connection.
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Main method for handling client requests.
         * Processes the request based on whether it is a GET or PUT request.
         */
        public void run() {
            try {
                // Initialize input and output streams
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                // Read and process the HTTP request
                String request = input.readLine();
                if (request.startsWith("GET")) {
                    handleGet(output);
                } else if (request.startsWith("PUT")) {
                    handlePut(input, output);
                } else {
                    // Unsupported request type
                    output.println("HTTP/1.1 400 Bad Request");
                }
            } catch (IOException e) {
                // Handle IO exception
                e.printStackTrace();
            } finally {
                // Close the socket connection
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Handles GET requests by returning weather data.
         *
         * @param output The output stream to send the response.
         * @throws IOException If an IO error occurs.
         */
        private void handleGet(PrintWriter output) throws IOException {
            // Send successful response header
            output.println("HTTP/1.1 200 OK");
            output.println("Content-Type: application/json");
            output.println();
            // Ensure weather data is not empty
            if (!weatherDataMap.isEmpty()) {
                // Send weather data in JSON format
                output.println(gson.toJson(weatherDataMap));
            } else {
                // Send an empty object if no weather data is available
                output.println("{}");
            }
        }

        /**
         * Handles PUT requests by storing weather data.
         *
         * @param input  The input stream to read the request body.
         * @param output The output stream to send the response.
         * @throws IOException If an IO error occurs.
         */
        private void handlePut(BufferedReader input, PrintWriter output) throws IOException {
            StringBuilder body = new StringBuilder();
            String line;
            boolean isBody = false;

            // Read the HTTP request and separate the JSON body
            while ((line = input.readLine()) != null) {
                if (line.isEmpty()) {
                    // Empty line indicates end of headers, start reading body
                    isBody = true;
                    continue;
                }

                if (isBody) {
                    // Read the JSON body part
                    body.append(line);
                }
            }

            // Debug output of received request body
            System.out.println("Received body: " + body.toString());

            try {
                // Parse JSON data into a WeatherData object
                WeatherData newData = gson.fromJson(body.toString(), WeatherData.class);

                // Validate and store weather data
                if (newData != null && newData.id != null) {
                    weatherDataMap.put(newData.id, newData);
                    // Confirm data reception success
                    output.println("HTTP/1.1 200 OK");
                } else {
                    // Invalid data
                    output.println("HTTP/1.1 400 Bad Request");
                }
            } catch (Exception e) {
                // Handle parsing errors
                e.printStackTrace();
                // Internal server error
                output.println("HTTP/1.1 500 Internal Server Error");
            }
        }
    }
}