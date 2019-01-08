package com.example.beng.mealcreditapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private static ArrayList<JSONObject> myAvailabilites = new ArrayList<JSONObject>();
    LinearLayout linearLayout;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        //System.out.println("HERE...");

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Button btnSettings = (Button) findViewById(R.id.title_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   //Intent intent = new Intent(MainActivity.this, settings.class);
                   //startActivity(intent);
                   setAvailabilities();
               }
           }
        );

        Button addAvailability = (Button) findViewById(R.id.add_availability);
        addAvailability.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(MainActivity.this, availability_dialog.class);
                   startActivity(intent);
               }
           }
        );

        linearLayout = findViewById(R.id.av_layout);
        //setAvailabilities();
    }

    private void setAvailabilities() {
        Thread av_thread = new Thread() {
            @Override
            public void run() {
                if(UserCheck.hasInitialized() || true) {
                    String userId = User.getUserId();
                    userId = "1";
                    if(!userId.equals("")) {
                        ServerCommunicationGet scg = new ServerCommunicationGet("availability-list/" + userId);
                        final Response response = scg.sendGetRequest();
                        if(response == null) {
                            return;
                        }

                        try {
                            if(response.code() == 200) {
                                JSONObject json = new JSONObject(response.body().string());
                                JSONArray jsonArray = json.getJSONArray("result");
                                final int amntOfPosts = jsonArray.length();
                                //System.out.println("Number of posts: " + amntOfPosts);
                                for(int i = 0; i < amntOfPosts; i++) {
                                    //System.out.println(jsonArray.getJSONObject(i).toString());
                                    addAndPostAvPost(jsonArray.getJSONObject(i));
                                }
                            }

                        }
                        catch (JSONException e) { System.out.println("JSon exception availability"); System.out.println(e.getMessage()); return; }
                        catch (IOException e) { System.out.println("IOexception availability"); return; }
                    }
                }
            }
        };
        av_thread.start();
    }

    private void addAndPostAvPost(JSONObject jsonObject) {
        myAvailabilites.add(jsonObject);
        //LinearLayout newPost = new LinearLayout(this, R.layout.availability_post);

        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout newPost = new LinearLayout(MainActivity.this);
                /*View post = LayoutInflater.from(newPost.getContext()).inflate(
                        R.layout.availability_post, newPost, true);
                newPost.addView(post);*/
                /*LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                LinearLayout post = (LinearLayout) inflater.inflate(R.layout.availability_post, newPost, false);
                newPost.addView(post);
                linearLayout.addView(newPost);
            }
        });*/

    }

    public static ArrayList<JSONObject> getAvPosts() {
        return myAvailabilites;
    }
}
