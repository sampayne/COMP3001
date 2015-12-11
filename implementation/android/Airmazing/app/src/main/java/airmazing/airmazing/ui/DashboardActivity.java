package airmazing.airmazing.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import airmazing.airmazing.R;
import airmazing.airmazing.models.UserSettings;
import airmazing.airmazing.views.FeedbackAvgView;
import airmazing.airmazing.views.FeedbackInputView;
import airmazing.airmazing.views.OverallAvgView;
import airmazing.airmazing.views.RouteIndexView;

public class DashboardActivity extends AppCompatActivity implements OnClickListener, SwipeRefreshLayout.OnRefreshListener{

    public RouteIndexView todayView;
    public RouteIndexView tomorrowView;

    public FeedbackAvgView feedbackAvgView;
    public FeedbackInputView feedbackInputView;
    public OverallAvgView overallAvgView;

    public SwipeRefreshLayout refreshLayout;

    public Button recordRouteButton;
    public Button settingsButton;

    public Boolean firstLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        UserSettings.context = getApplicationContext();


        todayView = (RouteIndexView) findViewById(R.id.todayView);

        todayView.IS_LIVE = true;

        tomorrowView = (RouteIndexView) findViewById(R.id.tomorrowView);
        feedbackAvgView = (FeedbackAvgView) findViewById(R.id.feedbackAvg);
        feedbackInputView = (FeedbackInputView) findViewById(R.id.feedbackInput);

        recordRouteButton = (Button) findViewById(R.id.record_route_button);
        settingsButton = (Button) findViewById(R.id.dashboardSettingsButtons);

        overallAvgView = (OverallAvgView) findViewById(R.id.overallView);

        feedbackAvgView.setOnClickListener(this);
        recordRouteButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);

        this.refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        refreshLayout.setOnRefreshListener(this);

        this.onRefresh();

    }

    public void onRefresh(){

        overallAvgView.startLoading();
        tomorrowView.startLoading();
        todayView.startLoading();
        feedbackInputView.startLoading();
        feedbackAvgView.startLoading();
        refreshLayout.setRefreshing(false);
    }

    public void onClick(View v){

        if (v.getId() == settingsButton.getId()){
            settingsButtonClick();
        }else if (v.getId() == recordRouteButton.getId()){
            recordRouteButtonClick();
        }else if(v.getId() == todayView.getId()){
            feedbackModuleSelect();
        }else if(v.getId() == feedbackAvgView.getId()){
            feedbackModuleSelect();
        }
    }

    public void recordRouteButtonClick(){

        startActivity(new Intent(this, RouteRecordActivity.class));
    }

    public void feedbackModuleSelect(){

        this.startActivity(new Intent(this, FeedbackListActivity.class));
    }

    public void settingsButtonClick(){

        this.startActivity(new Intent(this, SettingsActivity.class));

    }

    @Override
    public void onResume(){

        super.onResume();

        if (UserSettings.defaultRouteID() > 0){

            tomorrowView.setVisibility(View.VISIBLE);
            todayView.setVisibility(View.VISIBLE);
            tomorrowView.start();
            todayView.start();
            recordRouteButton.setVisibility(View.GONE);

        }else{

            recordRouteButton.setVisibility(View.VISIBLE);
            tomorrowView.setVisibility(View.GONE);
            todayView.setVisibility(View.GONE);
        }

        overallAvgView.start();
        feedbackInputView.start();
        feedbackAvgView.start();
        firstLoad = true;

    }

    @Override
    public void onStop(){

        super.onStop();

        overallAvgView.pause();
        tomorrowView.pause();
        todayView.pause();
        feedbackInputView.pause();
        feedbackAvgView.pause();
    }

    @Override
    public void onPause(){

        super.onPause();

        overallAvgView.pause();
        tomorrowView.pause();
        todayView.pause();
        feedbackInputView.pause();
        feedbackAvgView.pause();
    }
}
