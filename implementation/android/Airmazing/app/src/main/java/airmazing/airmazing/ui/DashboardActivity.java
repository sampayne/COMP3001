package airmazing.airmazing.ui;

import android.app.AlertDialog;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.view.View.OnClickListener;
import android.view.View;

import android.content.Intent;
import android.widget.Button;

import airmazing.airmazing.R;
import airmazing.airmazing.views.FeedbackAvgView;
import airmazing.airmazing.views.FeedbackInputView;
import airmazing.airmazing.views.RouteIndexView;

public class DashboardActivity extends AppCompatActivity implements OnClickListener{

    public RouteIndexView todayView;
    public RouteIndexView tomorrowView;

    public FeedbackAvgView feedbackAvgView;
    public FeedbackInputView feedbackInputView;

    public Button recordRouteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        todayView = (RouteIndexView) findViewById(R.id.todayView);
        tomorrowView = (RouteIndexView) findViewById(R.id.tomorrowView);
        feedbackAvgView = (FeedbackAvgView) findViewById(R.id.feedbackAvg);
        feedbackInputView = (FeedbackInputView) findViewById(R.id.feedbackInput);
        recordRouteButton = (Button) findViewById(R.id.record_route_button);

        feedbackAvgView.setOnClickListener(this);
        recordRouteButton.setOnClickListener(this);
    }


    public void onClick(View v){


        if (v.getId() == recordRouteButton.getId()){

                recordRouteButtonClick();

        }else if(v.getId() == todayView.getId()){

                feedbackModuleSelect();
        }

    }

    public void recordRouteButtonClick(){

        startActivity(new Intent(this, RouteRecordActivity.class));

    }

    public void leaveFeedbackButtonClick(){

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Placeholder alert - Should bring up feedback input view");
        alert.show();
    }

    public void chartModuleSelect(){

        this.startActivity(new Intent(this, HistoricalChartActivity.class));
    }

    public void feedbackModuleSelect(){

        this.startActivity(new Intent(this, FeedbackListActivity.class));
    }

}
