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
public class Species {
    public static final String JSON_URL = Main.API_URL + "/Information/Species/Json";
    public static final String XML_URL = Main.API_URL + "/Information/Species";

    public static final String JSON_FILE = Main.DOWNLOAD_FOLDER + "species/json/species.txt";
    public static final String XML_FILE = Main.DOWNLOAD_FOLDER + "species/xml/species.xml";

    public static void download() throws IOException {
        Files.downloadFile(
                JSON_URL,
                JSON_FILE
        );
    }

    public static Entry parse(JSONObject o) {
        Entry species = new Entry();

        species.setAttribute("species_id", o.getString("@SpeciesCode"));
        species.setAttribute("name", o.getString("@SpeciesName"));
        species.setAttribute("description", o.getString("@Description"));
        species.setAttribute("health_effect", o.getString("@HealthEffect"));
        species.setAttribute("londonair_url", o.getString("@Link"));

        return species;
    }

    public static List<Entry> parse() {
        return parse(new File(JSON_FILE));
    }

    public static List<Entry> parse(File file) {

        JSONArray speciesJson = Files.getJsonObject(file).getJSONObject("AirQualitySpecies").getJSONArray("Species");

        List<Entry> speciesList = new ArrayList<Entry>();

        for (int i = 0; i < speciesJson.length(); i++) {
            Entry species = parse(speciesJson.getJSONObject(i));
            speciesList.add(species);
        }

        return speciesList;
    }

    public static void outputToPostgres() {
        Entries.outputEntriesToPostgres(parse(), "londonair1", "species");
    }
}
