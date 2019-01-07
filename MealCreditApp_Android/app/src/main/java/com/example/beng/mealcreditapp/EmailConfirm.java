package com.example.beng.mealcreditapp;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import okhttp3.Response;

public class EmailConfirm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_confirm);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setElevation(0);

        Bundle b = getIntent().getExtras();
        String iPrefs = ""; // or other values
        if(b != null) {
            iPrefs = b.getString("user_info");
            setUpPage(iPrefs);
        }
        else {
            Intent intent = new Intent(this, signup.class);
            startActivity(intent);
            finish();
        }
        final String prefs = iPrefs;

        Button btnConfirmed = (Button) findViewById(R.id.email_confirm_but);
        btnConfirmed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonLogin;
                String email;
                String password;
                try {
                   jsonLogin  = new JSONObject(prefs);
                    email = jsonLogin.getString("email");
                    password = jsonLogin.getString("password");

                } catch (JSONException e) { System.out.println("JSon expcetion email"); return; }

                final String emailToUse = email;
                final String passwordToUse = password;

                Thread trySignIn = new Thread() {
                    @Override
                    public void run() {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("email", emailToUse);
                            json.put("password", passwordToUse);
                        } catch (JSONException e) {}

                        final String jsonText = json.toString();
                        //System.out.println("JSON string: " + jsonText);
                        ServerCommunicationPost scp = new ServerCommunicationPost("login/", jsonText);
                        final Response response = scp.sendPostRequest();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(response.code() == 401) {
                                    DoAlert.doBasicAlert("Please enter valid login credentials!", EmailConfirm.this);
                                }
                                else if(response.code() == 200) {
                                    //DoAlert.doBasicAlert("Sign in successful!", login.this);
                                    try {
                                        String res = response.body().string();
                                        JSONObject jsonRes = new JSONObject(res);
                                        User.setUser(jsonRes.getString("token"), jsonRes.getString("user_id"), jsonRes.getString("username"),
                                                jsonRes.getString("firstname"), jsonRes.getString("lastname"));
                                        UserCheck.setUserSharedPreferences(jsonRes);
                                        Intent intent = new Intent(EmailConfirm.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();

                                    } catch(Exception e) { return; }
                                }
                            }
                        });
                    }
                };
                trySignIn.start();
            }
        });

        Button btnCancel = (Button) findViewById(R.id.email_cancel_but);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EmailConfirm.this, signup.class);
                Bundle b = new Bundle();
                b.putString("user_info", prefs);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
                finish();
            }
        });
    }

    private void setUpPage(String strPrefs) {
        TextView tv = (TextView) findViewById(R.id.tv_depth_message);
        tv.setText(R.string.email_confirm_text2);

        TextView tv2 = (TextView) findViewById(R.id.tv_message);

        try {
            String emailGiven = new JSONObject(strPrefs).getString("email");
            String strToDisplay = tv2.getText().toString() + " at:\n " + emailGiven;
            tv2.setText(strToDisplay);
        }
        catch (JSONException e) { System.out.println("JSON exception on email confirmation"); }

        Button topBut = (Button) findViewById(R.id.email_confirm_but);
        topBut.setText(R.string.email_confirm_confirm);

        Button bottomBut = (Button) findViewById(R.id.email_cancel_but);
        bottomBut.setText(R.string.email_confirm_cancel);
    }
}
