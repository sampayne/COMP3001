package airmazing.airmazing.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import airmazing.airmazing.R;
import airmazing.airmazing.models.Feedback;
import airmazing.airmazing.networking.APIClient;
import airmazing.airmazing.networking.Callback;
import airmazing.airmazing.views.FeedbackRow;

public class FeedbackListActivity extends AppCompatActivity{

    private ArrayList<ImageButton> buttons = new ArrayList<>();
    public APIClient api;



    public LinearLayout rootLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_list);

        this.api = new APIClient(getApplicationContext());

       rootLayout = (LinearLayout) findViewById(R.id.rootView);


    }


    protected void onResume(){

        super.onResume();

        this.api.feedback(new Callback<JSONObject>() {
            @Override
            public void response(JSONObject response) {
                try {

                    JSONObject feedback = response.getJSONObject("feedback");
                    JSONArray feedbackRatings = feedback.getJSONArray("feedbacks");
                    ArrayList<Feedback> feedbackArray = Feedback.fromArray(feedbackRatings);

                    updateRows(feedbackArray);

                } catch (JSONException e) {
                }

            }
        });


    }

    public void updateRows(ArrayList<Feedback> feedbackList){
        rootLayout.removeAllViews();

        for(int i = 0; i < 31 ; i++){

            LocalDate date = new LocalDate();
            date = date.minusDays(i);

            Number rating = 0;

            for(int y = 0; y < feedbackList.size(); y++){

                Feedback feedbackDay = feedbackList.get(y);

                if (feedbackDay.date.equals(date)){

                    rating = feedbackDay.rating;
                }
            }

            FeedbackRow row = (FeedbackRow) getLayoutInflater().inflate(R.layout.feedback_list_row,rootLayout, false);

            row.updateFeedback(date, rating);

            CardView.LayoutParams params = new CardView.LayoutParams( CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);

            params.setMargins(20, 10, 20, 10);

            row.setLayoutParams(params);

            if (i % 2 == 0) {
                row.setCardBackgroundColor(Color.WHITE);
            }else{

                row.setCardBackgroundColor(Color.parseColor("#eeeeee"));

            }


            this.rootLayout.addView(row, params);

        }
    }



}
