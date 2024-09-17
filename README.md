# Assignment 2
# Weather System
#### Zhao Yanbo a1950939
## Project Overview

This project implements a client-server system that aggregates and distributes weather data in JSON format using a RESTful API. The project includes Lamport clocks for logical clock synchronization across multiple clients and servers. The system consists of:
- An **AggregationServer** that stores weather data and responds to client requests.
- A **ContentServer** that sends new weather data to the **AggregationServer**.
- A **GETClient** that fetches the stored weather data from the **AggregationServer**.

All interactions between clients and servers include Lamport clock values to ensure the correct logical order of events.

## Features

- **RESTful API**: Clients communicate with the server using HTTP GET and PUT requests.
- **JSON Format**: All weather data is transmitted and stored in JSON format.
- **Lamport Clocks**: Each client and server uses Lamport clocks to maintain logical time and ensure the correct ordering of events in distributed systems.
- **Persistent Storage**: The **AggregationServer** stores weather data persistently during the session.


## Project Structure

The project consists of the following files:

- `AggregationServer.java`: The server that aggregates and distributes weather data.
- `ContentServer.java`: Sends weather data in JSON format to the **AggregationServer**.
- `GETClient.java`: Fetches and displays the weather data stored on the **AggregationServer**.
- `LamportClock.java`: Manages the Lamport clock logic for synchronizing logical time.
- `WeatherData.java`: Represents the weather data model used by the server and client.

## Setup
## Installation and Setup

### 1. Change to the project directory:
```bash
cd WeatherSystem
```
### 2. Build the project using Maven:
```bash
mvn clean compile
```
### 3. Start the Aggregation Server:
The AggregationServer listens on port 4567 by default, but you can specify a different port as an argument. Run the server using the following command
    
```bash
mvn exec:java -Dexec.mainClass=org.example.AggregationServer
```
or specify a port:
```bash
mvn exec:java -Dexec.mainClass=org.example.AggregationServer -Dexec.args="<port>"
```
### 4. Start the Content Server:
The ContentServer sends weather data from a specified file to the AggregationServer. Run the server with the following command
```bash
mvn exec:java -Dexec.mainClass=org.example.ContentServer -Dexec.args="localhost:4567 weather.txt"
```

### 5 Run the GET Client:
The GETClient fetches weather data from the AggregationServer. Run the client using
```bash
mvn exec:java -Dexec.mainClass=org.example.GETClient -Dexec.args="localhost:4567"
```
This retrieves the stored weather data from the server at localhost:4567.

## Input/Output
### Step 1: Uploading Weather Data
There is a file called weather.txt in the project with the following content:
```text
id: IDS60901
name: Adelaide (West Terrace / ngayirdapira)
state: SA
time_zone: CST
lat: -34.9
lon: 138.6
local_date_time: 15/04:00pm
local_date_time_full: 20230715160000
air_temp: 13.3
apparent_t: 9.5
cloud: Partly cloudy
dewpt: 5.7
press: 1023.9
rel_hum: 60
wind_dir: S
wind_spd_kmh: 15
wind_spd_kt: 8
```
Or put a new file in the project and change the file name in the Content Server command.
### Step 2: Run the ContentServer to upload this data to the AggregationServer:
```bash
mvn exec:java -Dexec.mainClass=org.example.ContentServer -Dexec.args="localhost:4567 weather.txt"
```
You may see the following output:
```text
Sending JSON: {"id":"IDS60901","name":"Adelaide (West Terrace / ngayirdapira)","state":"SA",
"time_zone":"CST","lat":-34.9,"lon":138.6,"local_date_time":"15/04","local_date_time_full":
"20230715160000","air_temp":13.3,"apparent_t":9.5,"cloud":"Partly cloudy","dewpt":5.7,
"press":1023.9,"rel_hum":60,"wind_dir":"S","wind_spd_kmh":15,"wind_spd_kt":8}
```
### Step 3: Run the GETClient to fetch the weather data:
```bash
mvn exec:java -Dexec.mainClass=org.example.GETClient -Dexec.args="localhost:4567"
```
On the GETClient terminal, the following output will be displayed:
```text
Server response body: {"IDS60901":{"id":"IDS60901","name":"Adelaide (West Terrace / ngayirdapira)",...}}
Weather for: Adelaide (West Terrace / ngayirdapira)
Temperature: 13.3°C
Cloud: Partly cloudy
Wind Speed: 15 km/h
```

## Expected Output in AggregationServer
If you stop the GETClient abruptly, the AggregationServer will display a SocketException due to connection reset:
```text
java.net.SocketException: Connection reset
    at ...
Client disconnected
```
