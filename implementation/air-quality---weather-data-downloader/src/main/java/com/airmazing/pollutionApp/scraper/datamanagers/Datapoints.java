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
 * Created by andrei on 02/11/15.
 */
public class Datapoints {

    public static final String SITES_AIR_QUALITY_JSON_URL = Main.API_URL + "/Data/SiteSpecies/SiteCode={siteCode}/SpeciesCode={speciesCode}/StartDate={startDate}/EndDate={endDate}/Json";
    public static final String SITES_AIR_QUALITY_XML_URL = Main.API_URL + "/Data/SiteSpecies/SiteCode={siteCode}/SpeciesCode=NO2/StartDate={startDate}/EndDate=02-11-2015";



    public static String prepareUrl (String unpreparedUrl, String siteId, String speciesId, Date startDate, Date endDate) {

        String preparedUrl = unpreparedUrl.replace("{siteCode}", siteId);
        preparedUrl = preparedUrl.replace("{speciesCode}", speciesId);

        DateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        preparedUrl = preparedUrl.replace("{startDate}", format.format(startDate));
        preparedUrl = preparedUrl.replace("{endDate}", format.format(endDate));

        return preparedUrl;

    }

    public static void download(String siteId, String speciesId, Date startDate, Date endDate) throws IOException {
        String url = prepareUrl(SITES_AIR_QUALITY_JSON_URL, siteId, speciesId, startDate, endDate);
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        String dateString = format.format(startDate) + "_" + format.format(endDate);
        Files.downloadFile(
                url,
                Main.DOWNLOAD_FOLDER + "air quality/json/" + dateString + "/" + speciesId + "/" + siteId + "-" + speciesId + "-" + dateString + ".txt"
        );
    }

    public static void download(List<String> siteIds, String speciesId, Date startDate, Date endDate) throws IOException {
        int i = 0;
        for (String siteCode : siteIds) {
            System.out.print(++i + ". ");
            download(siteCode, speciesId, startDate, endDate);
        }
    }

    public static List<Entry> parse(JSONObject o) throws ParseException, JSONException {

        o = o.getJSONObject("RawAQData");

        String siteCode = o.getString("@SiteCode");
        String speciesCode = o.getString("@SpeciesCode");

        JSONArray datapoints = o.getJSONArray("Data");

        List<Entry> toInsert  = new ArrayList<Entry>();


        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < datapoints.length(); i++) {
            String dateString = datapoints.getJSONObject(i).getString("@MeasurementDateGMT");
            Date date = format.parse(dateString);

            String value = datapoints.getJSONObject(i).getString("@Value");



            if (!value.equals("")) {
                Entry datapoint = new Entry();

                datapoint.setAttribute("site_id", siteCode);
                datapoint.setAttribute("species_id", speciesCode);
                datapoint.setAttribute("date", dateString);
                datapoint.setAttribute("value", value);

                //System.out.println(datapoint);

                toInsert.add(datapoint);
            }



        }

        return toInsert;

    }

    public static void insertToDb (Date startDate, Date endDate, String speciesId) throws ParseException, JSONException {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = format.format(startDate) + "_" + format.format(endDate);

        List<String> filesToInsert = Files.getFileNames(Main.DOWNLOAD_FOLDER + "air quality/json/" + dateString + "/" + speciesId + "/");
        List<Entry> toInsert = new ArrayList<Entry>();

        for (String fileName : filesToInsert) {
            System.out.println(fileName);

            JSONObject o = Files.getJsonObject(new File(Main.DOWNLOAD_FOLDER + "air quality/json/" + dateString + "/" + speciesId + "/" + fileName));
            List<Entry> datapoints = parse(o);
            toInsert.addAll(datapoints);
        }
        Entries.outputEntriesToPostgres(toInsert, "londonair1", "pollution");
    }

}
