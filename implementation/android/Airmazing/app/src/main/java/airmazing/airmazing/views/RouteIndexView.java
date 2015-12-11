package airmazing.airmazing.views;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.TimerTask;

import airmazing.airmazing.R;
import airmazing.airmazing.models.AQIIndex;
import airmazing.airmazing.models.ExposureIndex;
import airmazing.airmazing.models.UserSettings;
import airmazing.airmazing.networking.Callback;


/**
 * Created by Sam on 02/12/2015.
 */
public class RouteIndexView extends Card implements View.OnClickListener{

    public TextView routeIndexTextView;
    public TextView homeIndexTextView;
    public TextView timeTextView;

    public LinearLayout homeWrapper;
    public LinearLayout routeWrapper;

    public LinearLayout moreDetailsContainer;
    public ImageButton viewDetails;

    public TextView commuteDetailsTextView;
    public TextView homeDetailtsTextView;

    public boolean IS_LIVE = false;

    public RouteIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void onClick(View v){

        Log.d("RouteIndexView", "viewMoreDetailsClicked");

        ImageButton button = (ImageButton) v;

        if (moreDetailsContainer.getVisibility() == View.GONE){

            button.setImageResource(R.drawable.arrow_up);

            moreDetailsContainer.setVisibility(View.VISIBLE);

        }else{

            moreDetailsContainer.setVisibility(View.GONE);
            button.setImageResource(R.drawable.arrow_down);

        }

    }



    @Override
    protected void onFinishInflate(){

        super.onFinishInflate();

        routeIndexTextView = (TextView) findViewById(R.id.routeIndexTextView);
        homeIndexTextView = (TextView) findViewById(R.id.homeIndexView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        homeWrapper = (LinearLayout) findViewById(R.id.homeWrapper);
        routeWrapper = (LinearLayout) findViewById(R.id.routeWrapper);

        homeDetailtsTextView = (TextView) findViewById(R.id.homeDetailsTextView);
        commuteDetailsTextView = (TextView) findViewById(R.id.commuteDetailsTextView);

        viewDetails = (ImageButton) findViewById(R.id.moreDetailsButton);
        viewDetails.setOnClickListener(this);

        moreDetailsContainer = (LinearLayout) findViewById(R.id.moreDetailsContainers);

        this.reloadTimerTask = new TimerTask() {
            @Override
            public void run() {


                final DateTime now = DateTime.now();

                if (IS_LIVE){

                    api.indexRoute(UserSettings.defaultRouteID(), now, UserSettings.defaultWorkTime(), new Callback<JSONObject>() {
                        @Override
                        public void response(JSONObject response) {

                            update(now, ExposureIndex.fromAPIResponse(response));
                            endLoading();
                        }
                    });

                    return;
                }

                DateTime next9am = now.withTime(UserSettings.defaultCommuteStartTime().getHourOfDay(), UserSettings.defaultCommuteStartTime().getMinuteOfHour(), 0, 0 );;

                if(now.toLocalTime().isAfter(UserSettings.defaultCommuteStartTime())){

                    next9am = next9am.plusDays(1);

                }

                final DateTime tomorrow = next9am;

                api.indexRoute(UserSettings.defaultRouteID(), tomorrow, UserSettings.defaultWorkTime(), new Callback<JSONObject>() {
                    @Override
                    public void response(JSONObject response) {

                        update(tomorrow, ExposureIndex.fromAPIResponse(response));
                        endLoading();
                    }
                });

            }
        };

    }

    public void update(DateTime date, ExposureIndex index){

        if (IS_LIVE){

            timeTextView.setText("Your Work Day - Live");

        }else if ((date.toLocalDate()).equals(new LocalDate())){

            timeTextView.setText("Your Work Day - Today " + date.toLocalTime().toString("HH:mma"));

        }else if((date.toLocalDate()).equals(new LocalDate().plusDays(1))){

            timeTextView.setText("Your Work Day - Tomorrow " + date.toLocalTime().toString("HH:mma"));

        }
        this.commuteDetailsTextView.setText("");
        this.homeDetailtsTextView.setText("");


        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);

        if(index.stayAtHomeAvg == null || index.stayAtHomeAvg.doubleValue() == 0){

            this.homeIndexTextView.setText("?/10");
            this.homeWrapper.setBackgroundColor(AQIIndex.BLUE);

        }else {

            this.homeWrapper.setBackgroundColor(AQIIndex.colorFromValue(index.stayAtHomeAvg.doubleValue()));
            this.homeIndexTextView.setText(df.format(index.stayAtHomeAvg.doubleValue()) + "/10");
            this.homeDetailtsTextView.setText("Staying at home today you will be exposed to \""+ AQIIndex.stringFromValue(index.stayAtHomeAvg.doubleValue()) +"\" levels of pollution.");
        }

        if(index.workAvg == null || index.workAvg.doubleValue() == 0){

            this.routeIndexTextView.setText("?/10");
            this.routeWrapper.setBackgroundColor(AQIIndex.BLUE);

        }else{

            this.commuteDetailsTextView.setText("Going to work today you will be exposed to \"Moderate\" levels of pollution.");
            this.routeWrapper.setBackgroundColor(AQIIndex.colorFromValue(index.workAvg.doubleValue()));
            this.routeIndexTextView.setText(df.format(index.workAvg.doubleValue()) + "/10");
        }

    }

}
