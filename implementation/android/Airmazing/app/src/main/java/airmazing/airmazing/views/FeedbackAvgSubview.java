package airmazing.airmazing.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.LocalDate;

import airmazing.airmazing.R;
import airmazing.airmazing.models.FeedbackIcon;

/**
 * Created by Sam on 07/12/2015.
 */
public class FeedbackAvgSubview extends LinearLayout{

    private TextView dayLabel;
    private ImageView icon;

    public FeedbackAvgSubview(Context context, AttributeSet attrs) {

        super(context, attrs);

    }

    protected void onFinishInflate (){

        super.onFinishInflate();

        dayLabel = (TextView) findViewById(R.id.feedbackSubViewLabel);
        icon = (ImageView) findViewById(R.id.feedbackSubViewIcon);

    }

    public void updateFeedback(LocalDate date, Number rating){

        if (rating.doubleValue() == 0.0){

            icon.setAlpha((float)0.3);
            dayLabel.setAlpha((float)0.3);

        }else{

            icon.setAlpha((float)1.0);
            dayLabel.setAlpha((float) 1.0);
        }

        icon.setImageResource(FeedbackIcon.iconFromValue(rating));

        dayLabel.setText(date.dayOfWeek().getAsShortText());

    }


}
