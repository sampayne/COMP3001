package airmazing.airmazing.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

import airmazing.airmazing.R;
import airmazing.airmazing.networking.APIClient;

/**
 * Created by Sam on 08/12/2015.
 */
public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailField;
    private EditText passwordField;
    private EditText timeAtWork;
    private EditText timeBegin;


    private Button signupButton;

    private APIClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        this.emailField = (EditText) findViewById(R.id.signupEmail);
        this.passwordField = (EditText) findViewById(R.id.passwordField);

        this.timeAtWork = (EditText) findViewById(R.id.signUpWorkTime);
        this.timeBegin = (EditText) findViewById(R.id.signUpCommuteTime);

        this.timeBegin.setOnClickListener(this);

        this.signupButton = (Button) findViewById(R.id.signupSignupButton);
        this.signupButton.setOnClickListener(this);

        this.api = new APIClient(this);

    }


    public void selectTime(){

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                timeBegin.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();


    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.signupSignupButton:{

                //Do something

            }
            case R.id.signUpCommuteTime:{

                selectTime();

            }
        }
    }

}
