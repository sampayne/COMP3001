package airmazing.airmazing.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.TimerTask;

import airmazing.airmazing.R;
import airmazing.airmazing.models.AQIIndex;
import airmazing.airmazing.networking.Callback;

/**
 * Created by Sam on 08/12/2015.
 */
public class OverallAvgView extends Card implements View.OnClickListener {

    //POST /Pollution/overallAvgForDateTime

    public LinearLayout containerView;

    private TextView timeTextView;
    private TextView indexTextView;

    public LinearLayout moreDetailsContainer;
    public ImageButton viewDetails;

    private TextView detailsTextView;

    public OverallAvgView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void onClick(View v){

        Log.d("OverallAvgView", "viewMoreDetailsClicked");

        ImageButton button = (ImageButton) v;

        if (moreDetailsContainer.getVisibility() == View.GONE){

            button.setImageResource(R.drawable.arrow_up);

            moreDetailsContainer.setVisibility(View.VISIBLE);

        }else{

            moreDetailsContainer.setVisibility(View.GONE);
            button.setImageResource(R.drawable.arrow_down);

        }

    }

    protected void onFinishInflate(){

        super.onFinishInflate();

        this.reloadTimerTask = new TimerTask() {
            @Override
            public void run() {
                api.indexLondon(DateTime.now(), new Callback<JSONObject>() {
                    @Override
                    public void response(JSONObject response) {
                        try{
                            Number avg = response.getDouble("avg");
                            updateWithAvg(avg);
                            endLoading();
                        }catch (JSONException e){}
                    }
                });
            }
        };

        timeTextView = (TextView) findViewById(R.id.timeTextView);
        indexTextView = (TextView) findViewById(R.id.indexTextView);
        containerView = (LinearLayout) findViewById(R.id.wrapper);

        moreDetailsContainer = (LinearLayout) findViewById(R.id.moreDetailsContainers);
        viewDetails = (ImageButton) findViewById(R.id.moreDetailsButton);
        detailsTextView = (TextView) findViewById(R.id.detailsTextView);

        viewDetails.setOnClickListener(this);
    }


    public void updateWithAvg(Number avg){

        if(avg != null && avg.doubleValue() > 0){


            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(1);

            this.indexTextView.setText(df.format(avg.doubleValue()) + "/10");
            this.timeTextView.setText("London - Live ");
            this.containerView.setBackgroundColor(AQIIndex.colorFromValue(avg.doubleValue()));

            this.detailsTextView.setText("The average pollution level across London now, is considered \"" + AQIIndex.stringFromValue(avg.doubleValue()) +"\".");

        }else{

            this.indexTextView.setText("?");
            this.containerView.setBackgroundColor(AQIIndex.BLUE);

        }


    }

}
