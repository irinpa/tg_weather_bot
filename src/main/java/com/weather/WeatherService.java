/*
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class WeatherService {
    private static final String BASEURL = "http://api.openweathermap.org/data/2.5/";
    private static final String FORECASTPATH = "forecast/daily";
    private static final String CURRENTPATH = "weather";
    //public static final String METRICSYSTEM = "metric";
    private static final String APIIDEND = "&APPID=b7471425f3402223826558883c98da13";
    private static final String FORECASTPARAMS = "&cnt=3&units=metric";
    private static final String ALERTPARAMS = "&cnt=1&units=metric";
    private static final String CURRENTPARAMS = "&cnt=1&units=metric";

    private static volatile WeatherService instance; ///< Instance of this class


    private WeatherService() {
    }

    public static WeatherService getInstance() {
        WeatherService currentInstance;
        if (instance == null) {
            synchronized (WeatherService.class) {
                if (instance == null) {
                    instance = new WeatherService();
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }


    public String fetchWeatherForecast(String city) {

        String cityFound;
        String responseToUser = null;
        try {
            String completeURL = BASEURL + CURRENTPATH + "?" + city + CURRENTPARAMS + APIIDEND;
            CloseableHttpClient client = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            HttpGet request = new HttpGet(completeURL);

            CloseableHttpResponse response = client.execute(request);
            HttpEntity ht = response.getEntity();

            System.out.println(completeURL);

            BufferedHttpEntity buf = new BufferedHttpEntity(ht);
            String responseString;
            responseString = EntityUtils.toString(buf, "UTF-8");

            JSONObject jsonObject = new JSONObject(responseString);
            if (jsonObject.getInt("cod") == 200) {
                cityFound = jsonObject.getJSONObject("city").getString("name");
                responseToUser = cityFound;
            } else {
                responseToUser = "cityNotFound";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseToUser;
    }

    public static void main(String [] strings){

        System.out.println(instance.fetchWeatherForecast("London"));
    }
}
*/
