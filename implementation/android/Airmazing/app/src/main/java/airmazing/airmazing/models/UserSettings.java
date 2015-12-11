package airmazing.airmazing.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.joda.time.LocalTime;

/**
 * Created by Sam on 11/12/2015.
 */
public class UserSettings {

    public static Context context;

    private static String  prefString = "airmazing.preferences";

    private static String routeString = "route-id";
    private static String hourString = "hourstring";
    private static String minuteString = "minuteString";
    private static String timeString = "timeString";
    private static String sessionTokenString = "sessionTokenString";
    private static String userIDString = "userIDString";


    public static void setUserID(Number userID){
        if (context == null){
            return;
        }
        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(userIDString, userID.intValue());
        editor.commit();
    }

    public static void setSessionToken(String sessionToken){
        if (context == null){
            return;
        }
        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(sessionTokenString, sessionToken);
        editor.commit();


    }

    public static void setDefaultRouteID(Number routeID){
        if (context == null){
            return;
        }
        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(routeString, routeID.intValue());
        editor.commit();


    }


    public static void setDefaultCommuteStartTime(LocalTime time){
        if (context == null){
            return;
        }
        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(hourString, time.getHourOfDay());
        editor.putInt(minuteString, time.getMinuteOfHour());
        editor.commit();

    }


    public static void setDefaultWorkTime(Number duration){

        if (context == null){
            return;
        }

        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putFloat(timeString, duration.floatValue());
        editor.commit();


    }

    public static void logout(){
        if (context == null){
            return;
        }
        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove(routeString);
        editor.remove(hourString);
        editor.remove(minuteString);
        editor.remove(timeString);
        editor.remove(sessionTokenString);
        editor.remove(userIDString);

        editor.commit();
    }

    public static int userID(){
        if (context == null){
            return 0;
        }
        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);

        int userID = preferences.getInt(userIDString, 0);

        Log.d("UserSettings.userID", "ID reports as " + Integer.toString(userID));

        return userID;

    }

    public static boolean isLoggedIn(){
        if (context == null){
            return false;
        }
        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);

        return preferences.getString(sessionTokenString, "error") != "error" && preferences.getInt(userIDString,0) > 0;

    }

    public static int defaultRouteID(){
        if (context == null){
            return 0;
        }
        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);

        return preferences.getInt(routeString, 9);

    }


    public static LocalTime defaultCommuteStartTime(){
        if (context == null){
            return LocalTime.now().withHourOfDay(8).withMinuteOfHour(0).withSecondOfMinute(0);
        }
        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);

        return LocalTime.now().withHourOfDay(preferences.getInt(hourString, 8)).withMinuteOfHour(preferences.getInt(minuteString,0));

    }


    public static double defaultWorkTime(){
        if (context == null){
            return 8*60;
        }
        SharedPreferences preferences =  context.getSharedPreferences(prefString, Context.MODE_PRIVATE);
        return (int) (preferences.getFloat(timeString, 8) * 60.0);

    }
}
