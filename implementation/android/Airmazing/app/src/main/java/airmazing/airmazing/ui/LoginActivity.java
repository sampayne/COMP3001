package airmazing.airmazing.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.strongloop.android.loopback.AccessToken;
import com.strongloop.android.loopback.RestAdapter;
import com.strongloop.android.loopback.User;


import android.content.Intent;
import airmazing.airmazing.R;
import airmazing.airmazing.networking.UserRepository;

public class LoginActivity extends AppCompatActivity implements OnClickListener{

    private RestAdapter adapter;
    private UserRepository userRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.adapter = new RestAdapter(getApplicationContext(), "http://comp3001.sam-payne.co.uk:3000/api");
        this.userRepo = adapter.createRepository(UserRepository.class);

        Log.d("Message:", this.userRepo.getNameForRestUrl());

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.loginButton:{

                loginButtonClick();
            }
        }
    }

    public void loginButtonClick(){

        final EditText emailField = (EditText) findViewById(R.id.emailField);
        final EditText passwordField = (EditText) findViewById(R.id.passwordField);

        if (emailField.getText().length() > 0 && passwordField.getText().length() > 0){

            userRepo.loginUser(emailField.getText().toString(), passwordField.getText().toString(), new UserRepository.LoginCallBack() {

                @Override
                public void onSuccess(AccessToken token, User currentUser) {

                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    startActivity(intent);
                    finish();
                    System.out.println(token.getUserId() + ":" + currentUser.getId());
                }

                @Override
                public void onError(Throwable t) {
                    Log.e("Chatome", "Login E", t);
                }

            });



        }else{

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("You must enter your email and password");
            alert.show();
        }
    }
}
