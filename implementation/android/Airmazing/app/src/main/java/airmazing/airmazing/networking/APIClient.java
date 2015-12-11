package airmazing.airmazing.networking;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import airmazing.airmazing.models.Route;
import airmazing.airmazing.models.RoutePoint;
import airmazing.airmazing.models.UserSettings;

/**
 * Created by Sam on 06/12/2015.
 */
public class APIClient {

    public RequestQueue queue;

    public Context context;

    public String baseURL = "http://comp3001.sam-payne.co.uk:3000/api";

    public APIClient(Context context){

        super();

        this.context = context;
        this.queue = Volley.newRequestQueue(context);
    }

    private void addRequestToQueue(JsonRequest request){

        request.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        this.queue.add(request);

    }


    public void indexLondon( DateTime date,  final Callback<JSONObject> callback){

        String url = this.baseURL.concat("/Pollution/overallAvgForDateTime");
        JSONObject data = new JSONObject();

        try {

            data.put("datetime",date.toString());
        }catch (JSONException e){}

        Log.d("indexLondon", "Running request on: " + url + " Data: " + data.toString());


        JsonRequest request = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("indexLondon", response.toString());
                callback.response(response);
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });

        this.addRequestToQueue(request);
    }


    public void indexRoute(Number number, DateTime date, Number timeAtWork, final Callback<JSONObject> callback){

        if (number.intValue() == 0){
            return;

        }

        String url = this.baseURL.concat("/Routes/predictIndexGivenRouteID");

        JSONObject data = new JSONObject();

        try {

            data.put("routeid", number);
            data.put("numOfMinsStayTimeAtWork",timeAtWork);
            data.put("startdatetimeforprediction", date.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.d("indexRoute", "Running request on: " + url + " Data: " + data.toString());

        JsonRequest request = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("indexRoute", response.toString());
                callback.response(response);
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });

        this.addRequestToQueue(request);

    }

    public void uploadRoute(ArrayList<RoutePoint> route, final Callback<JSONObject> callback){

        String url = this.baseURL.concat("/Routes");


        JSONObject finalData = new JSONObject();
        JSONArray routeArray = new JSONArray();

        for (int i = 0; i < route.size() ; i ++){

            JSONObject data = new JSONObject();
            RoutePoint point = route.get(i);

            try {

                data.put("time", point.dateTime.toString("YYYY-MM-dd HH:mm:ss"));
                data.put("lat",point.location.getLatitude());
                data.put("lng",point.location.getLongitude());

            } catch (JSONException e) {
                e.printStackTrace();
            }

            routeArray.put(data);
        }

        try {

            finalData.put("route", routeArray);
            finalData.put("userid",UserSettings.userID());

        } catch (JSONException e) {}


        JsonRequest request = new JsonObjectRequest(Request.Method.POST, url, finalData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("updateFeedback", response.toString());


                callback.response(response);
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });

        this.addRequestToQueue(request);

    }

    public void updateFeedback(LocalDate date, Number score, final Callback<JSONObject> callback){

        String url = this.baseURL.concat("/Feedbacks");

        JSONObject data = new JSONObject();

        try {

            data.put("rating", score);
            data.put("dateuserid", date.toString().concat(",").concat(Integer.toString(UserSettings.userID())));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonRequest request = new JsonObjectRequest(Request.Method.PUT, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("updateFeedback", response.toString());
                callback.response(response);
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });

        this.addRequestToQueue(request);

    }

    public void feedback(final Callback<JSONObject> callback){

        String url = this.baseURL.concat("/Feedbacks/getbyuserid");

        JSONObject data = new JSONObject();

        try {
            data.put("userid", UserSettings.userID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("feedback", "Running request on: " + url + " Data: " + data.toString());

        JsonRequest request = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.response(response);
            }
        },new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
            }
        });

        this.addRequestToQueue(request);

    }



    public void login(String email, String password, final Callback<JSONObject> callback) {

        String url = this.baseURL.concat("/airmazingusers/login");

        JSONObject data = new JSONObject();

        try {
            data.put("password", password);
            data.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            UserSettings.setUserID(response.getInt("userId"));
                            UserSettings.setSessionToken(response.getString("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        callback.response(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());
                    }
                });

        this.addRequestToQueue(jsObjRequest);

    }
}
