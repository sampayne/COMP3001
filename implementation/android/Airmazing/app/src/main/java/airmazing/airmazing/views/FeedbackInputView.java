package airmazing.airmazing.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TimerTask;

import airmazing.airmazing.R;
import airmazing.airmazing.models.Feedback;
import airmazing.airmazing.networking.APIClient;
import airmazing.airmazing.networking.Callback;

/**
 * Created by Sam on 02/12/2015.
 */
public class FeedbackInputView extends Card implements OnClickListener {

    public static int defaultShowTime = 0;

    private ArrayList<ImageButton> buttons = new ArrayList<>();

    public FeedbackInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private final double FACE_ALPHA = 0.5;

    @Override
    public void onFinishInflate(){

        super.onFinishInflate();

        this.reloadTimerTask = new TimerTask() {
            @Override
            public void run() {
                api.feedback(new Callback<JSONObject>() {
                    @Override
                    public void response(JSONObject response) {

                        try {

                            JSONObject feedback = response.getJSONObject("feedback");
                            JSONArray feedbackRatings = feedback.getJSONArray("feedbacks");
                            ArrayList<Feedback> feedbackArray = Feedback.fromArray(feedbackRatings);
                            update(feedbackArray);
                            endLoading();

                        } catch (JSONException e) {}
                    }
                });

            }
        };

        System.out.println("onFinishInflate Called");

        this.buttons.add((ImageButton) this.findViewById(R.id.feedbackButton1));
        this.buttons.add((ImageButton) this.findViewById(R.id.feedbackButton2));
        this.buttons.add((ImageButton) this.findViewById(R.id.feedbackButton3));
        this.buttons.add((ImageButton) this.findViewById(R.id.feedbackButton4));
        this.buttons.add((ImageButton) this.findViewById(R.id.feedbackButton5));

        for(int i = 0; i < this.buttons.size(); i++){
            ImageButton button = this.buttons.get(i);
            button.setOnClickListener(this);
            button.setAlpha((float) FACE_ALPHA);
        }
    }

    public void update(ArrayList<Feedback> feedback){

        boolean shouldHide = false;

        for(int y = 0; y < feedback.size(); y++){

            Feedback feedbackDay = feedback.get(y);

            if (feedbackDay.date.equals(LocalDate.now()) && feedbackDay.rating.doubleValue() > 0){

               shouldHide = true;
            }
        }

        if(DateTime.now().getHourOfDay() >= this.defaultShowTime && !shouldHide){
            setVisibility(View.VISIBLE);
        }else{
            setVisibility(View.GONE);
        }

    }

    public void onClick(View v){

        for(int i = 0; i < buttons.size(); i++){
            ImageButton button = buttons.get(i);
            button.setAlpha((float) FACE_ALPHA);
        }

        Integer buttonIndex = buttons.indexOf(v);

        v.setAlpha((float) 1.0);

        Log.d("onClick", "Button " + buttonIndex.toString());

        this.api.updateFeedback(new LocalDate(), buttonIndex + 1, new Callback<JSONObject>() {
            @Override
            public void response(JSONObject response) {
                setVisibility(View.GONE);
            }
        });
    }
}
