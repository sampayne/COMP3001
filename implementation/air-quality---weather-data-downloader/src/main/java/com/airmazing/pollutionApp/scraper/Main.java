package com.airmazing.pollutionApp.scraper;

import com.airmazing.pollutionApp.scraper.datamanagers.Datapoints;
import com.airmazing.pollutionApp.scraper.datamanagers.Sites;
import com.airmazing.pollutionApp.scraper.datamanagers.Weather;
import com.airmazing.pollutionApp.scraper.objects.Entries;
import com.airmazing.pollutionApp.scraper.objects.Entry;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by andrei on 27/10/15.
 */
public class Main {

    public static final String DOWNLOAD_FOLDER = System.getProperty("user.dir") + "/output/downloads/";
    public static final String LONDONAIR_URL = "http://www.londonair.org.uk/london/asp/datadownload.asp";

    public static final String API_URL = "http://api.erg.kcl.ac.uk/AirQuality";

    public static final String SITES_JSON_URL = API_URL + "/Information/MonitoringSites/GroupName=London/Json";
    public static final String SITES_XML_URL = API_URL + "/Information/MonitoringSites/GroupName=London";

    public static final String SITES_AIR_QUALITY_JSON_URL = API_URL + "/Data/SiteSpecies/SiteCode={siteCode}/SpeciesCode=NO2/StartDate=01-01-2015/EndDate=01-10-2015/Json";
    public static final String SITES_AIR_QUALITY_XML_URL = API_URL + "/Data/SiteSpecies/SiteCode={siteCode}/SpeciesCode=NO2/StartDate=01-11-2015/EndDate=02-11-2015";

    public static boolean LOG_DOWNLOADS = true;


    public static void downloadDatapoints (String speciesId) throws IOException {
        List<String> siteIds = Entries.getDifferentAttributeValues("site_id", Sites.parse());
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date startDate = format.parse("2015-11-03");
            Date endDate = format.parse("2015-12-02");

            Datapoints.download(siteIds, speciesId, startDate, endDate);

/*            startDate = format.parse("2014-01-01");
            endDate = format.parse("2015-01-01");

            Datapoints.download(siteIds, speciesId, startDate, endDate);

            startDate = format.parse("2013-01-01");
            endDate = format.parse("2014-01-01");

            Datapoints.download(siteIds, speciesId, startDate, endDate);*/

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void downloadWeather () throws IOException {

        String query = "SELECT dp.site_id, s.lat, s.lng from (SELECT DISTINCT site_id from pollution) dp left join site s on s.site_id = dp.site_id";

        List<Entry> sites = Entries.inputEntriesFromPostgres("londonair1", query);

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        for (Entry site : sites) {

            System.out.println(site.getAttribute("site_id"));

            try {
                Date startDate = format.parse("2015-11-01");

                Calendar cal = Calendar.getInstance();
                cal.setTime(startDate);

                for (int i = 11; i <= 11; i++) {
                    cal.add(Calendar.MONTH, 1);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    Date endDate = cal.getTime();
                    Weather.download(site, startDate, endDate);

                    TimeUnit.MILLISECONDS.sleep(220);

                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    startDate = cal.getTime();
                }

            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static void getYesterdayAirQuality(String speciesId) throws IOException, ParseException {
        List<String> siteIds = Entries.getDifferentAttributeValues("site_id", Sites.parse());

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = cal.getTime();

        Datapoints.download(siteIds, speciesId, yesterday, today);

        Datapoints.insertToDb(yesterday, today, speciesId);
    }




    public static void main(String[] args) throws ParseException {

        //Sites.downloadXml();
        //Sites.outputToPostgres();

        //Species.download();
        //Species.outputToPostgres();

        //JSONObject datapointsJson = getJsonObject(new File(DOWNLOAD_FOLDER + "air quality/json/2014/NO2/BL0-NO2-2014.txt"));

        //System.out.println(getJsonRefineries(new File(DOWNLOAD_FOLDER + "sites/sites.txt")));

        try {
            getYesterdayAirQuality("PM10");
            getYesterdayAirQuality("PM25");
            getYesterdayAirQuality("NO2");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Datapoints.insertToDb();

      /*  try {
            downloadWeather();
        } catch (IOException e) {

            e.printStackTrace();
        }
*/
        //Weather.parse();
        //Weather.outputToPostgres();
    }

}
