package airmazing.airmazing.views;


import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import airmazing.airmazing.R;

/**
 * Created by Sam on 02/12/2015.
 */
public class FeedbackInputView extends LinearLayout{

    public ImageButton button1;
    public ImageButton button2;
    public ImageButton button3;
    public ImageButton button4;
    public ImageButton button5;

    public FeedbackInputView(Context context, AttributeSet attrs) {
        super(context, attrs);


        Log.d("FeedbackInputView:", "Created");

        button1 = (ImageButton) findViewById(R.id.button1);
        button2 = (ImageButton) findViewById(R.id.button2);
        button3 = (ImageButton) findViewById(R.id.button3);
        button4 = (ImageButton) findViewById(R.id.button4);
        button5 = (ImageButton) findViewById(R.id.button5);


    }





}
