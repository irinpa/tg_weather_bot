package com.weather;

import com.google.gson.Gson;
import com.weather.model.CurrentWeather;
import com.weather.model.Forecast;
import com.weather.model.Main;
import com.weather.model.Weather;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class WeatherApiClient {
    private static final String BASEURL = "http://api.openweathermap.org/data/2.5/";
    private static final String CURRENTPATH = "weather";
    private static final String FORECASTPATH = "forecast";
    private static final String APIIDEND = "&APPID=b7471425f3402223826558883c98da13";
    private static final String CURRENTPARAMS = "&cnt=1&units=metric";
    private Gson gson;

    public WeatherApiClient() {
        gson = new Gson();
    }

    public <T> T getResponseFromOWM(String query, Class<T> clazz) throws Exception {
        // TODO weather vs forecast url + ?
        String urlString = BASEURL + CURRENTPATH + "?" + query + CURRENTPARAMS + APIIDEND;

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
        String query = CURRENTPATH + "lat=" + latitude + "&lon=" + longitude;
        return getByQuery(query);
    }

    public String getWeatherByText(String text) throws Exception {
        String query = CURRENTPATH + "q=" + text;
        return getByQuery(query);
    }

    public String getForecastByText(String text) throws Exception {
        String query = FORECASTPATH + "q=" + text;
        Forecast forecast = getResponseFromOWM(query, Forecast.class);
        List<Weather> weathers = forecast.getWeather();
        Weather weather = weathers.get(0);  // TODO why always 0
        Main main = forecast.getMain();
        StringBuilder sb = new StringBuilder();
        return sb.append("City: " + forecast.getName()).append("\n")
                .append("Condition: " + weather.getDescription()).append("\n")
                .append("Temperature: " + (main.getTemp().toString()) + "°C")
                .toString();
    }

    private String getByQuery(String query) throws Exception {
        CurrentWeather currentWeather = getResponseFromOWM(query, CurrentWeather.class);
        return weatherToString(currentWeather);
    }

    private String weatherToString(CurrentWeather currentWeather) {
        List<Weather> weathers = currentWeather.getWeather();
        Weather weather = weathers.get(0);  // TODO why always 0
        Main main = currentWeather.getMain();
        StringBuilder sb = new StringBuilder();
        return sb.append("City: " + currentWeather.getName()).append("\n")
                .append("Condition: " + weather.getDescription()).append("\n")
                .append("Temperature: " + (main.getTemp().toString()) + "°C")
                .toString();
    }
}




