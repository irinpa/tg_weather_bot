import com.google.gson.Gson;
import com.weather.CurrentWeather;
import com.weather.Main;
import com.weather.Weather;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Send_HTTP_Request {


    private static final String BASEURL = "http://api.openweathermap.org/data/2.5/";
    private static final String CURRENTPATH = "weather";
    private static final String FORECASTPATH = "forecast";
    private static final String APIIDEND = "&APPID=b7471425f3402223826558883c98da13";
    private static final String CURRENTPARAMS = "&cnt=1&units=metric";


    public CurrentWeather call_me(String query) throws Exception {
        String urlString = BASEURL + CURRENTPATH + "?" + query + CURRENTPARAMS + APIIDEND;

        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        System.out.println("Sending 'GET' request to URL : " + url);

        System.out.println("Response code: " + urlConnection.getResponseCode());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println("JSON: " + response.toString());

        Gson gson = new Gson();
        return gson.fromJson((response.toString()), CurrentWeather.class);
    }

    public String getByLocation(Float latitude, Float longitude) throws Exception {
        String query = "lat=" + latitude + "&lon=" + longitude;
        return getByQuery(query);
    }

    public String getByText(String text) throws Exception {
        String query = "q=" + text;
        return getByQuery(query);
    }

    private String getByQuery(String query) throws Exception {
        CurrentWeather currentWeather = call_me(query);
        List<Weather> weathers = currentWeather.getWeather();
        Weather weather = weathers.get(0);
        Main main = currentWeather.getMain();
        StringBuilder sb = new StringBuilder();
        return sb.append("City: " + currentWeather.getName()).append("\n")
                .append("Condition: " + weather.getDescription()).append("\n")
                .append("Temperature: " + (main.getTemp().toString()) + "°C")
                .toString();
    }
}




