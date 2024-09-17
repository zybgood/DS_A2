package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.net.*;
import java.lang.reflect.Type;
import java.util.Map;

public class GETClient {
    private static final Gson gson = new Gson();

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

            // 发送 GET 请求
            out.println("GET /weather.json HTTP/1.1");

            // 读取服务器的响应
            String responseLine;
            StringBuilder response = new StringBuilder();
            boolean isBody = false;

            while ((responseLine = in.readLine()) != null) {
                if (responseLine.isEmpty()) {
                    // 空行表示 HTTP 头的结束
                    isBody = true;
                    continue;
                }
                if (isBody) {
                    response.append(responseLine);  // 只读取正文,为了转换成JSON 数据
                }
            }

            // 打印服务器响应的正文
            System.out.println("Server response body: " + response.toString());

            // 使用 TypeToken 指定 Map<String, WeatherData> 类型，解决WeatherDataMap 的类型转换问题
            Type weatherDataType = new TypeToken<Map<String, WeatherData>>() {}.getType();
            Map<String, WeatherData> weatherDataMap = gson.fromJson(response.toString(), weatherDataType);
            displayWeather(weatherDataMap);

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
