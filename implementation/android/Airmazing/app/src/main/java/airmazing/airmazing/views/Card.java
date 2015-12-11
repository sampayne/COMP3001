package airmazing.airmazing.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import airmazing.airmazing.R;
import airmazing.airmazing.networking.APIClient;

/**
 * Created by Sam on 10/12/2015.
 */
public class Card extends CardView{

    public LinearLayout loadingView;
    public LinearLayout containerView;

    public Timer reloadTimer = new Timer();
    public TimerTask reloadTimerTask;

    public static int DEFAULT_RELOAD_TIME_SECONDS = 30;

    public APIClient api;

    public Card(Context context, AttributeSet attrs) {

        super(context, attrs);

        if (isInEditMode()){

            return;
        }

        this.api = new APIClient(context);
    }

    public void pause(){

        this.reloadTimer.cancel();

    }

    public void start(){

       /* this.reloadTimerTask.run();

        this.reloadTimer.cancel();

        this.reloadTimer = new Timer();


        this.reloadTimer.scheduleAtFixedRate(this.reloadTimerTask, 0, DEFAULT_RELOAD_TIME_SECONDS * 1000);*/
    }

    public void startLoading(){

        this.reloadTimerTask.run();
        this.loadingView.setVisibility(View.VISIBLE);
    }

    public void endLoading(){

        this.loadingView.setVisibility(View.GONE);

    }

    protected void onFinishInflate (){

        super.onFinishInflate();

        loadingView = (LinearLayout) findViewById(R.id.loadingView);
        containerView = (LinearLayout) findViewById(R.id.containerView);

    }


}
