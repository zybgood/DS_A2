package org.example;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AggregationServer {
    // A ConcurrentHashMap storing weather data with string keys and WeatherData values
    private static Map<String, WeatherData> weatherDataMap = new ConcurrentHashMap<>();

    // The port number the server listens on
    private static int port = 4567;

    // A Gson instance for JSON serialization and deserialization
    private static final Gson gson = new Gson();

    // A LamportClock instance for generating Lamport timestamps
    private static LamportClock lamportClock = new LamportClock();


    /**
     * The entry point of the application.
     * This method starts a server that listens on a specified port and handles client connections.
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
            System.out.println("Server is running on port " + port);
            // Infinite loop to accept client connections
            while (true) {
                // When a client connection is accepted, create a ClientHandler object and start it to handle the connection
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            // Handle IO exception
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
         * @param socket The client's socket connection.
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Main method for handling client requests.
         * 1. Initializes input and output streams.
         * 2. Reads and parses the client request, extracts "Lamport-Clock", and updates the server's Lamport clock.
         * 3. Calls the appropriate handler method based on the request type (GET or PUT).
         * 4. Returns the server's Lamport clock to the client.
         */
        public void run() {
            try {
                // Initialize input and output streams
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                // Read the request line from the client
                String request = input.readLine();

                // If the request is null, the client may have disconnected
                if (request == null) {
                    return;
                }

                // Read and parse HTTP headers to find "Lamport-Clock"
                String line;
                int clientClock = 0;
                while (!(line = input.readLine()).isEmpty()) {
                    if (line.startsWith("Lamport-Clock:")) {
                        clientClock = Integer.parseInt(line.split(":")[1].trim());
                    }
                }

                // Update the server's Lamport clock
                lamportClock.update(clientClock);

                // Call the appropriate handler method based on the request type
                if (request.startsWith("GET")) {
                    handleGet(output);
                } else if (request.startsWith("PUT")) {
                    handlePut(input, output);
                } else {
                    output.println("HTTP/1.1 400 Bad Request");
                }

                // Return the server's Lamport clock in the response
                output.println("Lamport-Clock: " + lamportClock.getClock());
                output.flush();  // Ensure data is sent to the client
            } catch (IOException e) {
                System.out.println("Client disconnected");
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
         * Handles GET requests.
         *
         * @param output The output stream to send responses to the client.
         * @throws IOException If an I/O error occurs.
         */
        private void handleGet(PrintWriter output) throws IOException {
            // Send a successful response header
            output.println("HTTP/1.1 200 OK");
            output.println("Content-Type: application/json");
            output.println();

            // Send stored weather data, or an empty JSON object if none exists
            if (!weatherDataMap.isEmpty()) {
                output.println(gson.toJson(weatherDataMap));
            } else {
                output.println("{}");
            }

            // Increment the Lamport clock
            lamportClock.increment();
        }

        /**
         * Handles PUT requests.
         *
         * @param input  The input stream to read data from the client.
         * @param output The output stream to send responses to the client.
         * @throws IOException If an I/O error occurs.
         */
        private void handlePut(BufferedReader input, PrintWriter output) throws IOException {
            // Read the request body data
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null && !line.isEmpty()) {
                body.append(line);
            }

            // Parse JSON data and store it in the map
            WeatherData newData = gson.fromJson(body.toString(), WeatherData.class);
            if (newData != null && newData.id != null) {
                weatherDataMap.put(newData.id, newData);
                output.println("HTTP/1.1 200 OK");
            } else {
                output.println("HTTP/1.1 400 Bad Request");
            }

            // Increment the Lamport clock
            lamportClock.increment();
        }
    }

}
