package com.weather;

import com.google.gson.Gson;
import com.weather.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class WeatherApiClient {
    private static final String BASEURL = "http://api.openweathermap.org/data/2.5/";
    private static final String CURRENTWEATHERPATH = "weather?cnt=1&";
    private static final String FORECASTPATH = "forecast?cnt=40&";
    private static final String APIIDEND = "&APPID=b7471425f3402223826558883c98da13";
    private static final String UNITMETRIC = "&units=metric";
    private Gson gson;

    public WeatherApiClient() {
        gson = new Gson();
    }

    public <T> T getResponseFromOWM(String query, Class<T> clazz) throws Exception {

        String urlString = BASEURL + query + UNITMETRIC + APIIDEND;

        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        System.out.println("Sending 'GET' request to URL : " + url);
        System.out.println("Response code: " + con.getResponseCode());

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            System.out.println("JSON: " + response.toString());
            return gson.fromJson((response.toString()), clazz);
        }
    }

    public String getWeatherByLocation(Float latitude, Float longitude) throws Exception {
        String query = CURRENTWEATHERPATH + "lat=" + latitude + "&lon=" + longitude;
        return getCurrentWeatherByQuery(query);
    }

    public String getWeatherByText(String text) throws Exception {
        String query = CURRENTWEATHERPATH + "q=" + text;
        return getCurrentWeatherByQuery(query);
    }

    public String getForecastByText(String topic) throws Exception {
        String query = FORECASTPATH + "q=" + topic;
        return getForecastByQuery(query);
    }

    public String getForecastByLocation(Float latitude, Float longitude) throws Exception {
        String query = FORECASTPATH + "lat=" + latitude + "&lon=" + longitude;
        return getForecastByQuery(query);
    }

    private String getForecastByQuery(String query) throws Exception {
        Forecast forecast = getResponseFromOWM(query, Forecast.class);
        return forecastToString(forecast);
    }

    private String getCurrentWeatherByQuery(String query) throws Exception {
        CurrentWeather currentWeather = getResponseFromOWM(query, CurrentWeather.class);
        return weatherToString(currentWeather);
    }

    private String weatherToString(CurrentWeather currentWeather) {
        List<Weather> weathers = currentWeather.getWeather();
        Weather weather = weathers.get(0);  // TODO why always 0
        Main main = currentWeather.getMain();
        StringBuilder sb = new StringBuilder();
        return sb.append("Current weather for " + currentWeather.getName()).append(":\n\n")
                .append("Condition: " + weather.getDescription()).append("\n")
                .append("Temperature: " + (main.getTemp().toString()) + "°C").append("\n\n")
                .toString();
    }

    private String forecastToString(Forecast forecast) {
        List<TimeSlotWeather> forecastList = forecast.getList();

        List<List<TimeSlotWeather>> weatherByDays = groupForecastByDays(forecastList);

//        System.out.println("Forecast: " + weatherByDays.toString());

        StringBuilder sb = new StringBuilder();
        sb.append("Forecast for tomorrow in " + forecast.getCity().getName()).append(":\n\n");
        for (TimeSlotWeather weatherByDay : weatherByDays.get(1)) {
            ZonedDateTime time = getZonedDateTime(weatherByDay);

            sb.append("----  at ").append(time.getHour()).append(":").append(time.getMinute()).append("0").append("  ----\n");
            sb.append("Condition: " + weatherByDay.getWeather().get(0).getDescription()).append("\n");
            sb.append("Temperature: " + (weatherByDay.getMain().getTemp().toString()) + "°C");
            sb.append("\n\n");
        }

        return sb.toString();
    }

    private List<List<TimeSlotWeather>> groupForecastByDays(List<TimeSlotWeather> forecastList) {
        List<List<TimeSlotWeather>> weatherByDays = new ArrayList<>();
        ZonedDateTime lastTime = null;
        for (TimeSlotWeather weather : forecastList) {
            System.out.println("slot");
            ZonedDateTime time = getZonedDateTime(weather);
            if (lastTime == null || time.getDayOfYear() != lastTime.getDayOfYear()) {
                List<TimeSlotWeather> newDay = new ArrayList<>();
                newDay.add(weather);
                weatherByDays.add(newDay);
                System.out.println("new day");
            } else {
                System.out.println("same day");
                List<TimeSlotWeather> currentDay = weatherByDays.get(weatherByDays.size() - 1);
                currentDay.add(weather);
            }
            lastTime = time;
        }
        return weatherByDays;
    }

    private ZonedDateTime getZonedDateTime(TimeSlotWeather weatherByDay) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(weatherByDay.getDt()), ZoneId.systemDefault());
    }
}




