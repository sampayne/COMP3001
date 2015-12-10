package com.airmazing.pollutionApp.scraper.datamanagers;

import com.airmazing.pollutionApp.scraper.Main;
import com.airmazing.pollutionApp.scraper.objects.Entries;
import com.airmazing.pollutionApp.scraper.objects.Entry;
import com.airmazing.pollutionApp.scraper.utils.Files;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by andrei on 05/11/15.
 */
public class Weather {
    //1e27d9f1669f1cef172bb14e6e93e
    //4d6e67e6199216f06871bc327934e
    private static final String API_URL = "http://api.worldweatheronline.com/premium/v1/past-weather.ashx?q={lat}%2C{lng}&format=json&extra=localObsTime&date={startDate}&enddate={endDate}&includelocation=yes&tp=1&key=1e27d9f1669f1cef172bb14e6e93e";

    public static String prepareUrl (String unpreparedUrl, String lat, String lng, Date startDate, Date endDate) {

        String preparedUrl = unpreparedUrl.replace("{lat}", lat);
        preparedUrl = preparedUrl.replace("{lng}", lng);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        preparedUrl = preparedUrl.replace("{startDate}", format.format(startDate));
        preparedUrl = preparedUrl.replace("{endDate}", format.format(endDate));

        return preparedUrl;

    }

    public static void download(Entry site, Date startDate, Date endDate) throws IOException {
        String url = prepareUrl(API_URL, site.getAttribute("lat"), site.getAttribute("lng"), startDate, endDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        Files.downloadFile(
                url,
                Main.DOWNLOAD_FOLDER + "weather/json/" + year + "/" + site.getAttribute("site_id") + "-" + month + "-" + year + ".txt",
                220
        );
    }

    public static void download(List<Entry> sites, Date startDate, Date endDate) throws IOException {
        int i = 0;
        for (Entry site : sites) {
            System.out.print(++i + ". ");
            download(site, startDate, endDate);
        }
    }

    public static List<Entry> parse(String siteId, JSONObject o) throws ParseException, JSONException {

        JSONArray weatherData = o.getJSONObject("data").getJSONArray("weather");

        List<Entry> weatherDatapoints  = new ArrayList<Entry>();


        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date;
        for (int i =0; i < weatherData.length(); i++) {
            date = dateFormat.parse(weatherData.getJSONObject(i).getString("date"));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            JSONArray dataList = weatherData.getJSONObject(i).getJSONArray("hourly");
            for (int j = 0; j < dataList.length(); j++) {

                JSONObject data = dataList.getJSONObject(j);

                Entry weatherDatapoint = new Entry();
                weatherDatapoint.setAttribute("site_id", siteId);

                weatherDatapoint.setAttribute("cloud_cover", data.getString("cloudcover"));
                weatherDatapoint.setAttribute("dew_point_c", data.getString("DewPointC"));
                weatherDatapoint.setAttribute("feels_like_c", data.getString("FeelsLikeC"));
                weatherDatapoint.setAttribute("heat_index_c", data.getString("HeatIndexF"));
                weatherDatapoint.setAttribute("humidity", data.getString("humidity"));
                weatherDatapoint.setAttribute("precip_mm", data.getString("precipMM"));
                weatherDatapoint.setAttribute("pressure", data.getString("pressure"));
                weatherDatapoint.setAttribute("temperature_c", data.getString("tempC"));
                weatherDatapoint.setAttribute("visibility", data.getString("visibility"));
                weatherDatapoint.setAttribute("wind_chill_c", data.getString("WindChillC"));
                weatherDatapoint.setAttribute("wind_direction_degree", data.getString("winddirDegree"));
                weatherDatapoint.setAttribute("wind_gust_kmph", data.getString("WindGustKmph"));
                weatherDatapoint.setAttribute("wind_speed_kmph", data.getString("windspeedKmph"));

                weatherDatapoint.setAttribute("date", dateTimeFormat.format(cal.getTime()));
                cal.add(Calendar.HOUR, 1);

                weatherDatapoints.add(weatherDatapoint);
            }

        }



        return weatherDatapoints;
    }

    public static List<Entry> parse(File file) throws ParseException, JSONException {
        JSONObject o = Files.getJsonObject(file);
        String fileName = file.getName();
        String siteId = fileName.split("-")[0];

        return parse(siteId, o);
    }

    public static List<Entry> parse() throws ParseException, JSONException {

        List<Entry> weatherDatapoints = new ArrayList<Entry>();
        List<String> filesToInsert = Files.getFileNames(Main.DOWNLOAD_FOLDER + "weather/json/" + 2015 + "/");
        for (String fileName : filesToInsert) {
            weatherDatapoints.addAll(parse(new File(Main.DOWNLOAD_FOLDER + "weather/json/" + 2015 + "/" + fileName)));
        }
        return weatherDatapoints;
    }

    public static void outputToPostgres() throws ParseException, JSONException {
        Entries.outputEntriesToPostgres(parse(), "londonair1", "weather");
    }
}
