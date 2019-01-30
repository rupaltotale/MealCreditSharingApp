package com.example.beng.mealcreditapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;

public class signup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setElevation(0);

        Button btnSignUp = (Button) findViewById(R.id.signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignUp();
            }
        });

        DoAlert.doBasicAlert("If you use email, you will have to confirm it. If you choose not too, however, you will not be able to recover your account on a forgotten password.", this);
    }


    private void doSignUp() {
        final String firstName = ((EditText)findViewById(R.id.firstname)).getText().toString();
        final String lastName = ((EditText)findViewById(R.id.lastname)).getText().toString();
        final String username = ((EditText)findViewById(R.id.username)).getText().toString();
        final String email = ((EditText)findViewById(R.id.email)).getText().toString();
        final String password = ((EditText)findViewById(R.id.password)).getText().toString();

        if(!email.equals("") && !UserCheck.validate(email)) {
            DoAlert.doBasicAlert("Please use a valid email address!", this);
            return;
        }
        if(UserCheck.validate(username)) {
            DoAlert.doBasicAlert("Do not use an email address as a username please!", this);
            return;
        }
        if(password.equals("")) {
            DoAlert.doBasicAlert("Please enter a password!", this);
        }
        if(email.equals("") && username.equals("")) {
            DoAlert.doBasicAlert("Please enter either a username or password!", this);
        }

        Thread newSignUpThread = new Thread() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    if(!firstName.equals("")) {
                        json.put("firstname",firstName);
                    }
                    if(!lastName.equals("")) {
                        json.put("lastname", lastName);
                    }
                    if(!username.equals("")) {
                        json.put("username", username);
                    }
                    if(!email.equals("")) {
                        json.put("email", email);
                    }
                    json.put("password", password);
                } catch (JSONException e) {}

                final String jsonText = json.toString();
                ServerCommunicationPost scp = new ServerCommunicationPost("register/", jsonText);
                final Response response = scp.sendPostRequest();

                try {
                    String res = response.body().string();
                    final JSONObject jsonRes = new JSONObject(res);

                    if (response.code() == 200 && email.equals("")) {
                        try {
                            String userId = jsonRes.get("user_id").toString();
                            String jwt = jsonRes.getString("token");
                            User.setUser(jwt, userId, username, firstName, lastName);
                            JSONObject forSharedPrefs = JsonMethods.makeJsonObjectFromStrings(new String[]{"firstname", "lastname", "username", "user_id", "jwt", "email"},
                                    new String[]{firstName, lastName, username, userId, jwt, email});
                            UserCheck.setUserSharedPreferences(forSharedPrefs);
                            Intent intent = new Intent(signup.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            System.out.println("JSON exception");
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (response.code() == 500) {
                                    DoAlert.doBasicAlert("Server error. We're sorry for the inconvenience. Please try again in a few minutes", signup.this);
                                }
                                else if (response.code() == 401) {
                                    try {
                                        DoAlert.doBasicAlert(jsonRes.getString("message"), signup.this);
                                    }
                                    catch (JSONException e) { System.out.println("JSON exception"); }
                                }
                                else if (response.code() == 200 && jsonRes.has("message")) {
                                    try {
                                        if (jsonRes.getString("message").equals("email sent")) {
                                            Intent intent = new Intent(signup.this, EmailConfirm.class);
                                            JSONObject forSharedPrefs = JsonMethods.makeJsonObjectFromStrings(new String[]{"firstname", "lastname", "username", "email", "password"},
                                                    new String[]{firstName, lastName, username, email, password});
                                            String jsonString = forSharedPrefs.toString();
                                            Bundle b = new Bundle();
                                            b.putString("user_info", jsonString);
                                            intent.putExtras(b); //Put your id to your next Intent
                                            startActivity(intent);
                                            finish();
                                        }
                                        else {
                                            Intent intent = new Intent(signup.this, MainActivity.class);
                                            JSONObject forSharedPrefs = JsonMethods.makeJsonObjectFromStrings(new String[]{"firstname", "lastname", "username", "email", "password"},
                                                    new String[]{firstName, lastName, username, email, password});
                                            String jsonString = forSharedPrefs.toString();
                                            Bundle b = new Bundle();
                                            b.putString("user_info", jsonString);
                                            intent.putExtras(b); //Put your id to your next Intent
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    catch (JSONException e) { System.out.println("JSON exception"); }
                                }
                            }
                        });
                    }
                }
                catch (IOException e) { System.out.println("IOException Occurred"); }
                catch (JSONException e) { System.out.println("JSON exception"); }
            }
        };
        newSignUpThread.start();
    }
}
