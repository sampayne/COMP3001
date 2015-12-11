package airmazing.airmazing.models;

import airmazing.airmazing.R;

/**
 * Created by Sam on 07/12/2015.
 */
public class FeedbackIcon {

    public static int ONE = R.drawable.very_sad;
    public static int TWO = R.drawable.sad;
    public static int THREE = R.drawable.indifferent;
    public static int FOUR = R.drawable.happy;
    public static int FIVE = R.drawable.very_happy;

    public static int iconFromValue(Number value){

        double doubleValue = value.doubleValue();

        if(doubleValue <= 0) {

            return THREE;

        }else if(doubleValue <= 1.0 && doubleValue > 0){

            return ONE;

        }else if(doubleValue <= 2.0){

            return TWO;

        }else if(doubleValue <= 3.0){

            return THREE;

        }else if(doubleValue <= 4.0){

            return FOUR;

        }else if(doubleValue <= 5.0){

            return FIVE;

        }

        return THREE;

    }

}

