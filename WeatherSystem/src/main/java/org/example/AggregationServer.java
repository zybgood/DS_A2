package org.example;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AggregationServer {
    private static Map<String, WeatherData> weatherDataMap = new ConcurrentHashMap<>();
    private static int port = 4567;
    private static final Gson gson = new Gson();
    private static LamportClock lamportClock = new LamportClock();

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

                if (request == null) {
                    return; // 客户端可能已经断开连接
                }

                // 读取并解析 HTTP 请求头，找到 "Lamport-Clock"
                String line;
                int clientClock = 0;
                while (!(line = input.readLine()).isEmpty()) {
                    if (line.startsWith("Lamport-Clock:")) {
                        clientClock = Integer.parseInt(line.split(":")[1].trim());
                    }
                }

                // 更新服务器的 Lamport 时钟
                lamportClock.update(clientClock);

                if (request.startsWith("GET")) {
                    handleGet(output);
                } else if (request.startsWith("PUT")) {
                    handlePut(input, output);
                } else {
                    output.println("HTTP/1.1 400 Bad Request");
                }

                // 在响应中返回服务器的 Lamport 时钟
                output.println("Lamport-Clock: " + lamportClock.getClock());
                output.flush();  // 确保输出流的数据发送到客户端
            } catch (IOException e) {
                System.out.println("Client disconnected");
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
            if (!weatherDataMap.isEmpty()) {
                output.println(gson.toJson(weatherDataMap));
            } else {
                output.println("{}");
            }

            // 增加 Lamport 时钟
            lamportClock.increment();
        }

        private void handlePut(BufferedReader input, PrintWriter output) throws IOException {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = input.readLine()) != null && !line.isEmpty()) {
                body.append(line);
            }

            WeatherData newData = gson.fromJson(body.toString(), WeatherData.class);
            if (newData != null && newData.id != null) {
                weatherDataMap.put(newData.id, newData);
                output.println("HTTP/1.1 200 OK");
            } else {
                output.println("HTTP/1.1 400 Bad Request");
            }

            // 增加 Lamport 时钟
            lamportClock.increment();
        }
    }
}
