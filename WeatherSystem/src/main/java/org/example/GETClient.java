package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Map;

public class GETClient {
    private static final Gson gson = new Gson();
    private static LamportClock lamportClock = new LamportClock();

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GETClient <server:port>");
            return;
        }

        String serverUrl = args[0];

        try {
            // 连接服务器
            Socket socket = new Socket(serverUrl.split(":")[0], Integer.parseInt(serverUrl.split(":")[1]));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 增加 Lamport 时钟
            lamportClock.increment();

            // 发送 GET 请求并带上 Lamport 时钟
            out.println("GET /weather.json HTTP/1.1");
            out.println("Lamport-Clock: " + lamportClock.getClock());
            out.println(); // 空行表示头结束

            // 读取服务器响应
            String responseLine;
            StringBuilder response = new StringBuilder();
            boolean isBody = false;

            while ((responseLine = in.readLine()) != null) {
                if (responseLine.isEmpty()) {
                    isBody = true;
                    continue;
                }
                if (isBody) {
                    response.append(responseLine);
                }
            }

            // 打印服务器响应
            System.out.println("Server response body: " + response.toString());

            // 处理响应，去掉 Lamport-Clock 部分
            String jsonResponse = response.toString().split("Lamport-Clock")[0].trim();

            // 使用 TypeToken 指定 Map<String, WeatherData> 类型
            Type weatherDataType = new TypeToken<Map<String, WeatherData>>() {}.getType();
            Map<String, WeatherData> weatherDataMap = gson.fromJson(jsonResponse, weatherDataType);
            displayWeather(weatherDataMap);

            // 关闭连接
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 显示天气数据
    private static void displayWeather(Map<String, WeatherData> dataMap) {
        for (String id : dataMap.keySet()) {
            WeatherData weather = dataMap.get(id);
            System.out.println("Weather for: " + weather.name);
            System.out.println("Temperature: " + weather.air_temp + "°C");
            System.out.println("Cloud: " + weather.cloud);
            System.out.println("Wind Speed: " + weather.wind_spd_kmh + " km/h");
        }
    }
}
