package airmazing.airmazing.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import airmazing.airmazing.R;


/**
 * Created by Sam on 02/12/2015.
 */
public class FeedbackAvgView extends LinearLayout {


    public ImageView avgIcon;
    public TextView avgText;

    public TextView day1Label;
    public ImageView day1icon;

    public TextView day2Label;
    public ImageView day2icon;

    public TextView day3Label;
    public ImageView day3icon;

    public TextView day4Label;
    public ImageView day4icon;

    public TextView day5Label;
    public ImageView day5icon;

    public TextView day6Label;
    public ImageView day6icon;

    public TextView day7Label;
    public ImageView day7icon;


    public FeedbackAvgView(Context context, AttributeSet attrs) {
        super(context, attrs);


            avgIcon = (ImageView) findViewById(R.id.feedbackAvgIcon);
            avgText = (TextView) findViewById(R.id.feedbackAvgText);


    }

    public void update(){




    }
}
