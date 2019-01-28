package com.example.beng.mealcreditapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.SharedPreferences;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnSignUp = (Button) findViewById(R.id.signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, signup.class);
                startActivity(intent);
            }
        });

        Button btnLogin = (Button) findViewById(R.id.loginbut);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailOrUsername = ((TextView) findViewById(R.id.login1)).getText().toString();
                final boolean isEmail = UserCheck.validate(emailOrUsername);
                final String password = ((TextView) findViewById(R.id.login2)).getText().toString();
                System.out.println("Attempting login");
                Thread trySignIn = new Thread() {
                    @Override
                    public void run() {

                        JSONObject json = new JSONObject();
                        try {
                            if(isEmail) {
                                json.put("email", emailOrUsername);
                            }
                            else {
                                json.put("username", emailOrUsername);
                            }
                            json.put("password", password);
                        } catch (JSONException e) {}

                        final String jsonText = json.toString();
                        //System.out.println("JSON string: " + jsonText);
                        ServerCommunicationPost scp = new ServerCommunicationPost("login/", jsonText);
                        final Response response = scp.sendPostRequest();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(response.code() == 401) {
                                    DoAlert.doBasicAlert("Please enter valid login credentials!", login.this);
                                }
                                else if(response.code() == 200) {
                                    //DoAlert.doBasicAlert("Sign in successful!", login.this);
                                    try {
                                        String res = response.body().string();
                                        JSONObject jsonRes = new JSONObject(res);
                                        User.setUser(jsonRes.getString("token"), jsonRes.get("user_id").toString(), jsonRes.getString("username"),
                                                jsonRes.getString("firstname"), jsonRes.getString("lastname"));
                                        UserCheck.setUserSharedPreferences(jsonRes);
                                        Intent intent = new Intent(login.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } catch(Exception e) {  }
                                }
                            }
                        });
                    }
                };
                trySignIn.start();
            }
        });

        //User u = new User("jwt", "7", "bglossner", "ben", "glossner", "12345");
        UserCheck.initializeUserCheck(getApplicationContext());
        boolean success = UserCheck.setUserInfoIfExists();
        if(success) {
            Intent intent = new Intent(login.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}