package airmazing.airmazing.views;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import airmazing.airmazing.R;

import android.widget.TextView;


/**
 * Created by Sam on 02/12/2015.
 */
public class RouteIndexView extends LinearLayout{

    public TextView routeIndexTextView;
    public TextView homeIndexTextView;
    public TextView timeTextView;

    public DateTime referenceDate;

    public RouteIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d("RouteIndexView:", "Created");

        routeIndexTextView = (TextView) findViewById(R.id.routeIndexTextView);
        homeIndexTextView = (TextView) findViewById(R.id.homeIndexView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);

    }

    public void update(){

        if ((referenceDate.toLocalDate()).equals(new LocalDate())){

            timeTextView.setText("Today");

        }else{

            timeTextView.setText("Tomorrow");

        }

        //run some networking code here.
    }

}
