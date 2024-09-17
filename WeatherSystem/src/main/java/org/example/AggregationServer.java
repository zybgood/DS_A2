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
                    handleGet(output);
                } else if (request.startsWith("PUT")) {
                    handlePut(input, output);
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

        private void handleGet(PrintWriter output) throws IOException {
            output.println("HTTP/1.1 200 OK");
            output.println("Content-Type: application/json");
            output.println();
            // Ensure the weather data map is not empty before returning it
            if (!weatherDataMap.isEmpty()) {
                output.println(gson.toJson(weatherDataMap));
            } else {
                // Return an empty object if no weather data is available
                output.println("{}");
            }
        }

        private void handlePut(BufferedReader input, PrintWriter output) throws IOException {
            StringBuilder body = new StringBuilder();
            String line;
            boolean isBody = false;

            // 读取 HTTP 请求，并跳过 headers，只处理 JSON 正文部分
            while ((line = input.readLine()) != null) {
                if (line.isEmpty()) {
                    // 空行表示 headers 的结束，接下来是请求正文
                    isBody = true;
                    continue;
                }

                if (isBody) {
                    // 开始读取 JSON 正文部分
                    body.append(line);
                }
            }

            // 打印接收到的正文内容（用于调试）
            System.out.println("Received body: " + body.toString());

            try {
                // 解析 JSON 正文
                WeatherData newData = gson.fromJson(body.toString(), WeatherData.class);

                if (newData != null && newData.id != null) {  // 确保数据有效
                    weatherDataMap.put(newData.id, newData);  // 存储天气数据
                    output.println("HTTP/1.1 200 OK");
                } else {
                    output.println("HTTP/1.1 400 Bad Request");  // 数据无效
                }
            } catch (Exception e) {
                // 处理解析错误
                e.printStackTrace();
                output.println("HTTP/1.1 500 Internal Server Error");
            }
        }


    }
}