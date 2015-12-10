package com.airmazing.pollutionApp.scraper.datamanagers;

import com.airmazing.pollutionApp.scraper.Main;
import com.airmazing.pollutionApp.scraper.objects.Entries;
import com.airmazing.pollutionApp.scraper.objects.Entry;
import com.airmazing.pollutionApp.scraper.utils.Files;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrei on 03/11/15.
 */
public class Sites {
    public static final String JSON_URL = Main.API_URL + "/Information/MonitoringSites/GroupName=London/Json";
    public static final String XML_URL = Main.API_URL + "/Information/MonitoringSites/GroupName=London";

    public static final String JSON_FILE = Main.DOWNLOAD_FOLDER + "sites/json/sites.txt";
    public static final String XML_FILE = Main.DOWNLOAD_FOLDER + "sites/xml/sites.xml";

    public static void download() throws IOException {
        Files.downloadFile(
                JSON_URL,
                JSON_FILE
        );
    }

    public static void downloadXml() throws IOException {
        Files.downloadFile(
                XML_URL,
                XML_FILE
        );
    }

    public static Entry parse(JSONObject o) {
        Entry site = new Entry();

        site.setAttribute("site_id", o.getString("@SiteCode"));
        site.setAttribute("name", o.getString("@SiteName"));
        site.setAttribute("type", o.getString("@SiteType"));
        site.setAttribute("lat", o.getString("@Latitude"));
        site.setAttribute("lng", o.getString("@Longitude"));
        site.setAttribute("date_opened", o.getString("@DateOpened"));
        site.setAttribute("date_closed", o.getString("@DateClosed"));

        return site;
    }

    public static List<Entry> parse() {
        return parse(new File(JSON_FILE));
    }

    public static List<Entry> parse(File file) {

        JSONArray sitesJson = Files.getJsonObject(file).getJSONObject("Sites").getJSONArray("Site");

        List<Entry> sites = new ArrayList<Entry>();

        for (int i = 0; i < sitesJson.length(); i++) {
            Entry site = parse(sitesJson.getJSONObject(i));
            sites.add(site);
        }

        return sites;
    }

    public static void outputToPostgres() {
        Entries.outputEntriesToPostgres(parse(), "londonair1", "site");
    }

}

