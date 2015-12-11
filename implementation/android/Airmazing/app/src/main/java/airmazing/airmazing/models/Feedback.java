package airmazing.airmazing.models;


import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Sam on 06/12/2015.
 */
public class Feedback {

    public String dateUserID;
    public Number userID;
    public LocalDate date;
    public Number rating;

    public Feedback(String dateUserID, Number rating) {

        this.rating = rating;

        String[] parts = dateUserID.split(",");

        this.dateUserID = dateUserID;

        this.date = LocalDate.parse(parts[0]);
        this.userID = Integer.parseInt(parts[1]);

    }

    public static ArrayList<Feedback> fromArray(JSONArray array){

        ArrayList<Feedback> returnArray = new ArrayList<>();

        for(int i = 0; i < array.length(); i++){

            try {

                JSONObject object = array.getJSONObject(i);
                Feedback feedback = new Feedback(object.getString("dateuserid"), object.getDouble("rating"));
                returnArray.add(feedback);

            }catch (JSONException e){}
        }

        return returnArray;


    }
}