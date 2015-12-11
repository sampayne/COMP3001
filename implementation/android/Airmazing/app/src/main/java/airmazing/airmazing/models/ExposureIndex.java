package airmazing.airmazing.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sam on 08/12/2015.
 */
public class ExposureIndex {

    public Number stayAtHome;
    public Number work;
    public Number stayAtHomeAvg;
    public Number workAvg;

    public static ExposureIndex fromAPIResponse(JSONObject json){

        ExposureIndex index = new ExposureIndex();

        try{

            JSONObject pollutionIndex = json.getJSONObject("pollutionExposureIndex");

            JSONObject stayAtHome = pollutionIndex.getJSONObject("stayingAtHome");
            JSONObject work = pollutionIndex.getJSONObject("goingToWork");

            index.stayAtHome = stayAtHome.getDouble("pollutionIndex");
            index.stayAtHomeAvg = stayAtHome.getDouble("averagePollutionIndex");

            index.work = work.getDouble("pollutionIndex");
            index.workAvg = work.getDouble("averagePollutionIndex");

            }catch(JSONException e){}

        return index;
    }
}
