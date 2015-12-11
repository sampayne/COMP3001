package airmazing.airmazing.views;

import android.content.Context;
import android.hardware.camera2.params.Face;
import android.media.Rating;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.json.JSONObject;

import java.util.ArrayList;

import airmazing.airmazing.R;
import airmazing.airmazing.models.Feedback;
import airmazing.airmazing.networking.APIClient;
import airmazing.airmazing.networking.Callback;

/**
 * Created by Sam on 11/12/2015.
 */
public class FeedbackRow extends CardView implements View.OnClickListener {

    private final double FACE_ALPHA = 0.3;

    private ArrayList<ImageButton> buttons = new ArrayList<>();
    public APIClient api;
    public TextView dateTextView;

    public LocalDate currentDate;

    public FeedbackRow(Context context, AttributeSet attrs) {

        super(context, attrs);
        this.api = new APIClient(context);

    }

    protected void onFinishInflate(){

        super.onFinishInflate();

        this.dateTextView = (TextView) findViewById(R.id.feedbackListRowText);
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

    public void updateFeedback(LocalDate date, Number rating){

        this.dateTextView.setText(date.toString("dd/MM/YY"));

        for(int i = 0; i < buttons.size(); i++){
            ImageButton button = buttons.get(i);

            if ((i + 1) == rating.intValue()){
                button.setAlpha((float) 1);
            }else{
                button.setAlpha((float) FACE_ALPHA);
            }
        }

        this.currentDate = date;

    }


    public void onClick(View v){

        for(int i = 0; i < buttons.size(); i++){
            ImageButton button = buttons.get(i);
            button.setAlpha((float) FACE_ALPHA);
        }

        Integer buttonIndex = buttons.indexOf(v);

        v.setAlpha((float) 1.0);

        Log.d("onClick", "Button " + buttonIndex.toString());

        this.api.updateFeedback(currentDate, buttonIndex + 1, new Callback<JSONObject>() {
            @Override
            public void response(JSONObject response) {


            }
        });
    }

}
