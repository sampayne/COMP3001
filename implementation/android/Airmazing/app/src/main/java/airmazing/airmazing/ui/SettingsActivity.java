package airmazing.airmazing.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import airmazing.airmazing.R;
import airmazing.airmazing.models.UserSettings;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    public Button saveButton;
    public Button recordRoute;
    public Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        saveButton = (Button) findViewById(R.id.saveSettingsButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        recordRoute = (Button) findViewById(R.id.settingsRecordNewRouteButton);

        recordRoute.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
    }

    public void recordNewRoute(){

        startActivity(new Intent(this, RouteRecordActivity.class));

    }

    public void onClick(View v){

        if (v.getId() == logoutButton.getId()){

            logoutButtonClick();

        }else if(v.getId() == recordRoute.getId()){

            recordNewRoute();

        }else if (v.getId() == saveButton.getId()){

            saveButtonClick();

        }

    }

    public void saveButtonClick(){


    }

    public void logoutButtonClick(){


        UserSettings.logout();

       Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
       intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       startActivity(intent);
    }

}
