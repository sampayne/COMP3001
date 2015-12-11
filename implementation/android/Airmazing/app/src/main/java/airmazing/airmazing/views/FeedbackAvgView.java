package airmazing.airmazing.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TimerTask;

import airmazing.airmazing.R;
import airmazing.airmazing.models.AQIIndex;
import airmazing.airmazing.models.Feedback;
import airmazing.airmazing.models.FeedbackIcon;
import airmazing.airmazing.networking.Callback;

/**
 * Created by Sam on 02/12/2015.
 */
public class FeedbackAvgView extends Card {

    private ImageView avgIcon;
    private TextView avgText;

    private ArrayList<FeedbackAvgSubview> dayViews = new ArrayList<>();

    public FeedbackAvgView(Context context, AttributeSet attrs) {

        super(context, attrs);

    }

    @Override
    protected void onFinishInflate (){

        super.onFinishInflate();

        this.reloadTimerTask = new TimerTask() {
            @Override
            public void run() {

                api.feedback(new Callback<JSONObject>() {
                    @Override
                    public void response(JSONObject response) {
                        Log.d("api.feedback", "responded");

                        Number average = 0;
                        ArrayList<Feedback> feedbackArray = new ArrayList<>();
                        try {

                            JSONObject feedback = response.getJSONObject("feedback");
                            JSONArray feedbackRatings = feedback.getJSONArray("feedbacks");
                            average = feedback.getDouble("avg");
                            feedbackArray = Feedback.fromArray(feedbackRatings);

                        } catch (JSONException e) {}

                        updateAvg(average);
                        updateDays(feedbackArray);
                        endLoading();

                    }
                });

            }
        };

        avgIcon = (ImageView) findViewById(R.id.feedbackAvgIcon);
        avgText = (TextView) findViewById(R.id.feedbackAvgText);

        dayViews.add((FeedbackAvgSubview) findViewById(R.id.day7));
        dayViews.add((FeedbackAvgSubview) findViewById(R.id.day6));
        dayViews.add((FeedbackAvgSubview) findViewById(R.id.day5));
        dayViews.add((FeedbackAvgSubview) findViewById(R.id.day4));
        dayViews.add((FeedbackAvgSubview) findViewById(R.id.day3));
        dayViews.add((FeedbackAvgSubview) findViewById(R.id.day2));
        dayViews.add((FeedbackAvgSubview) findViewById(R.id.day1));

    }

    public void updateDays(ArrayList<Feedback> feedback){

        for(int i = (dayViews.size() - 1); i >=0 ; i--){

            LocalDate date = new LocalDate();
            date = date.minusDays(i);

            Number rating = 0;

            for(int y = 0; y < feedback.size(); y++){

                Feedback feedbackDay = feedback.get(y);

                if (feedbackDay.date.equals(date)){

                    rating = feedbackDay.rating;
                }
            }

            FeedbackAvgSubview view = this.dayViews.get(i);

            view.updateFeedback(date, rating);

        }
    }

    public void updateAvg(Number avg){

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
        this.avgText.setText(df.format(avg.doubleValue()) + "/5 Avg.");

        this.setCardBackgroundColor(AQIIndex.colorFromRating(avg.doubleValue()));

        this.avgIcon.setImageResource(FeedbackIcon.iconFromValue(avg));
        this.avgIcon.setAlpha((float)1.0);

    }
}
