package org.example;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AggregationServer {
    private static Map<String, WeatherData> weatherDataMap = new ConcurrentHashMap<>();
    private static int port = 4567;
    private static long lastUpdateTime = System.currentTimeMillis();
    private static final int TIMEOUT = 30000; // 30 seconds timeout
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Thread to handle client requests (GET/PUT)
    static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                String request = input.readLine();
                if (request.startsWith("GET")) {
                    handleGet();
                } else if (request.startsWith("PUT")) {
                    handlePut();
                } else {
                    output.println("HTTP/1.1 400 Bad Request");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleGet() throws IOException {
            output.println("HTTP/1.1 200 OK");
            output.println("Content-Type: application/json");
            output.println();
            output.println(gson.toJson(weatherDataMap));
        }

        private void handlePut() throws IOException {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null && !line.isEmpty()) {
                body.append(line);
            }
            WeatherData newData = gson.fromJson(body.toString(), WeatherData.class);
            if (newData != null) {
                weatherDataMap.put(newData.id, newData);
                lastUpdateTime = System.currentTimeMillis();
                output.println("HTTP/1.1 200 OK");
            } else {
                output.println("HTTP/1.1 500 Internal Server Error");
            }
        }
    }
}

