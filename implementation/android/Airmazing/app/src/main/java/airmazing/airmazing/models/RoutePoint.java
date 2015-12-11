package airmazing.airmazing.models;

import android.location.Location;

import org.joda.time.DateTime;

/**
 * Created by Sam on 10/12/2015.
 */
public class RoutePoint {

    public DateTime dateTime;
    public Location location;

    public RoutePoint(DateTime time, Location location){

        this.dateTime = time;
        this.location = location;


    }

}
