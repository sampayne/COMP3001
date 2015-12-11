package airmazing.airmazing.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import airmazing.airmazing.R;
import airmazing.airmazing.models.UserSettings;
import airmazing.airmazing.networking.APIClient;
import airmazing.airmazing.networking.Callback;

public class LoginActivity extends AppCompatActivity implements OnClickListener{

    private EditText emailField;
    private EditText passwordField;

    private Button loginButton;
    private Button signUpButton;

    private APIClient api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        UserSettings.context = getApplicationContext();

        this.emailField = (EditText) findViewById(R.id.emailField);
        this.passwordField = (EditText) findViewById(R.id.passwordField);

        this.loginButton = (Button) findViewById(R.id.loginButton);
        this.loginButton.setOnClickListener(this);

        this.signUpButton = (Button) findViewById(R.id.loginSignUpButton);
        this.signUpButton.setOnClickListener(this);

        this.api = new APIClient(this);

    }

    private void signUpClick(){

        Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
        startActivity(intent);

    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.loginButton:{
                loginButtonClick();
                break;
            }
            case R.id.loginSignUpButton:{
                signUpClick();

            }
        }
    }

    public void loginButtonClick(){

        if (emailField.getText().length() > 0 && passwordField.getText().length() > 0){

            this.api.login(emailField.getText().toString(), passwordField.getText().toString(), new Callback<JSONObject>() {
                public void response(JSONObject response) {

                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

        }else{

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("You must enter your email and password");
            alert.show();
        }
    }

    @Override
    public void onResume(){

        super.onResume();

        if (UserSettings.isLoggedIn()) {
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(intent);
            finish();
        }

    }

}


