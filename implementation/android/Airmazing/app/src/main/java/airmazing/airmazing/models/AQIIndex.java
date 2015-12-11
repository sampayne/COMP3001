package airmazing.airmazing.models;

import android.graphics.Color;
import android.util.Log;

/**
 * Created by Sam on 07/12/2015.
 */
public abstract class AQIIndex {

    public final static int GREEN = Color.rgb(104,159,56);
    public final static int YELLOW = Color.rgb(253,216,52);
    public final static int ORANGE = Color.rgb(255,126,1);
    public final static int RED = Color.rgb(255,29,0);
    public final static int PURPLE = Color.rgb(153,18,76);
    public final static int MAROON = Color.rgb(126, 10, 36);

    public final static int BLUE = Color.parseColor("#2196F3");

    public final static double THRESHOLD_GREEN = 3.5;
    public final static double THRESHOLD_YELLOW = 5.5;
    public final static double THRESHOLD_ORANGE = 6.5;
    public final static double THRESHOLD_RED = 8.5;
    public final static double THRESHOLD_MAROON = 9.5;
    public final static double THRESHOLD_PURPLE = 10;

    public final static double MAX_VALUE = 500.0;

    public static int colorFromRating(Number value){

        double doubleValue = value.doubleValue();

        if(doubleValue < 1){
            return BLUE;
        }else if(doubleValue < 2){
            return RED;
        }else if(doubleValue < 3){
            return ORANGE;
        }else if(doubleValue < 4){
            return YELLOW;
        }else if(doubleValue < 5){
            return GREEN;
        }

        return Color.parseColor("#2196F3");
    }

    public static String stringFromValue(Number value){

        double doubleValue = value.doubleValue();

        if (doubleValue <= 0.5){
            return  "Error";
        }else if (doubleValue <= 3.5){
            return "Low";
        }else if (doubleValue <= 6.5){
            return "Moderate";
        }else if(doubleValue <= 9.5){
            return "High";
        }else if(doubleValue <= 10.5){
            return "Very High";
        }

        return "Unknown";

    }

    public static int colorFromValue(Number value) {

        double doubleValue = value.doubleValue();

        if (doubleValue <= 1){
            Log.d("colorFromValue", "Value:" + Double.toString(doubleValue) + " returning Blue");
            return  Color.parseColor("#2196F3");
        }else if (doubleValue <= THRESHOLD_GREEN){
            Log.d("colorFromValue", "Value:" + Double.toString(doubleValue) + " returning Green");

            return GREEN;
        }else if (doubleValue <= THRESHOLD_YELLOW){
            Log.d("colorFromValue", "Value:" + Double.toString(doubleValue) + " returning Yellow");

            return YELLOW;
        }else if(doubleValue <= THRESHOLD_ORANGE){
            Log.d("colorFromValue", "Value:" + Double.toString(doubleValue) + " returning Orange");

            return ORANGE;
        }else if(doubleValue <= THRESHOLD_RED){
            Log.d("colorFromValue", "Value:" + Double.toString(doubleValue) + " returning Red");

            return RED;
        }else if(doubleValue <= THRESHOLD_MAROON){
            Log.d("colorFromValue", "Value:" + Double.toString(doubleValue) + " returning Maroon");
            return MAROON;
        }else if(doubleValue <= THRESHOLD_PURPLE){
            Log.d("colorFromValue", "Value:" + Double.toString(doubleValue) + " returning Purple");

            return PURPLE;
        }


        Log.d("colorFromValue", "Value:" + Double.toString(doubleValue) + " returning Gray");
        return Color.GRAY;
    }

}
