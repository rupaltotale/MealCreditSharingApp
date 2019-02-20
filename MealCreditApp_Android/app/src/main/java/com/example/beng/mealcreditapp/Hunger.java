package com.example.beng.mealcreditapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.datatype.Duration;

import okhttp3.Response;

public class Hunger extends AppCompatActivity {

    private TextView mTextMessage;
    public static ArrayList<JSONObject> myHungerPosts = new ArrayList<JSONObject>();
    public static ArrayList<JSONObject> myMatches = new ArrayList<JSONObject>();
    LinearLayout linearLayout;
    int totalPosts;
    private boolean hungerPostsSet = false;
    private boolean onPostScreen = true;
    private static int NUM_START_POSTS = 3;
    private boolean resumed = false;
    private boolean hasBeenStarted = false;
    public static JSONObject newFilters;

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
        setContentView(R.layout.activity_hunger);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_bar_hg);
        setSupportActionBar(toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title_hg);

        mTextMessage = (TextView) findViewById(R.id.message);
        //BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Button btnSettings = (Button) findViewById(R.id.title_settings_hg);
        btnSettings.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(Hunger.this, settings.class);
                   startActivity(intent);
               }
           }
        );

        Button addHunger = (Button) findViewById(R.id.add_hunger);
        addHunger.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(Hunger.this, hunger_creation.class);
                   startActivity(intent);
               }
           });

        Button doFilter = findViewById(R.id.go_filter_page);
        doFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("LEAVING");
                Intent intent = new Intent(Hunger.this, FilterAvailability.class);
                startActivity(intent);
            }
        });

        Button postsScreen = (Button) findViewById(R.id.hg_posts_but);
        postsScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!onPostScreen) {
                    onPostScreen = true;
                    if(myHungerPosts.size() == 0 || resumed) {
                        removeAllPosts();
                        setHungerPosts();
                        resumed = false;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Button postsScreen = (Button) findViewById(R.id.hg_posts_but);
                            postsScreen.setBackgroundColor(getResources().getColor(R.color.white));
                            postsScreen.setTextColor(getResources().getColor(R.color.colorPrimary));
                            Button matchScreen = (Button) findViewById(R.id.hg_match_but);
                            matchScreen.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            matchScreen.setTextColor(getResources().getColor(R.color.white));
                        }
                    });
                }
            }
         }
        );
        Button matchScreen = (Button) findViewById(R.id.hg_match_but);
        matchScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onPostScreen) {
                    onPostScreen = false;
                    if(myMatches.size() == 0) {
                        removeAllPosts();
                        setMatches();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Button matchScreen = (Button) findViewById(R.id.hg_match_but);
                            matchScreen.setBackgroundColor(getResources().getColor(R.color.white));
                            matchScreen.setTextColor(getResources().getColor(R.color.colorPrimary));
                            Button postsScreen = (Button) findViewById(R.id.hg_posts_but);
                            postsScreen.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            postsScreen.setTextColor(getResources().getColor(R.color.white));
                        }
                    });
                }
            }
         }
        );

        linearLayout = findViewById(R.id.hg_layout);
        User.setUserId("15");
        setHungerPosts();
    }

    private void setHungerPosts() {
        Thread hg_thread = new Thread() {
            @Override
            public void run() {
                if(UserCheck.hasInitialized() || true) {
                    String userId = User.getUserId();
                    //System.out.println("User id: " + userId);
                    if(!userId.equals("")) {
                        ServerCommunicationGet scg = new ServerCommunicationGet("hunger-list/" + userId);
                        final Response response = scg.sendGetRequest();
                        //System.out.println(response);
                        if(response == null) {
                            return;
                        }

                        try {
                            System.out.println(response.code());
                            if(response.code() == 200) {
                                JSONObject json = new JSONObject(response.body().string());
                                JSONArray jsonArray = json.getJSONArray("result");
                                final int amntOfPosts = jsonArray.length();
                                totalPosts = amntOfPosts;
                                for(int i = 0; i < amntOfPosts; i++) {
                                    JSONObject newAvPost = jsonArray.getJSONObject(i);
                                    addAndPostHgPost(newAvPost, i, true);
                                }
                                hungerPostsSet = true;
                            }

                        }
                        catch (JSONException e) { System.out.println("JSon exception hunger"); System.out.println(e.getMessage()); return; }
                        catch (IOException e) { System.out.println("IOexception hunger"); return; }
                    }
                }
            }
        };
        hg_thread.start();
    }

    private void setMatches() {
        Thread get_matches = new Thread() {
            @Override
            public void run() {
                final String currentTime = DateParser.getCurrentDateTimeServer();
                ServerCommunicationGet scg = new ServerCommunicationGet("availability-list/" + NUM_START_POSTS +
                        "/false/false/" + currentTime + "/" +  currentTime +  "/false/asking_price");
                final Response response = scg.sendGetRequest();

                if(response == null) {
                    return;
                }

                try {
//                    /System.out.println(response.code());
                    if(response.code() == 200) {
                        String responseBody = response.body().string();
                        JSONObject matchObj = new JSONObject(responseBody);
                        JSONArray matches = matchObj.getJSONArray("result");
                        //System.out.println(matches.length());
                        for(int i = 0; i < matches.length(); i++) {
                            JSONObject newMatch = matches.getJSONObject(i);
                            addMatch(newMatch, currentTime);
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        get_matches.start();
    }

    private void addMatch(JSONObject newMatch, String currDate) {
        //System.out.println(JsonMethods.getString(newMatch, "end_time"));
        String readableTimeDifference = DateParser.getHumanTimeDifference(currDate, JsonMethods.getString(newMatch, "end_time"));
        if(readableTimeDifference.contains("0m")) {
            return;
        }
        LayoutInflater inflator = Hunger.this.getLayoutInflater();
        View rowView = inflator.inflate(R.layout.match_post_hg, null);
        ViewGroup vg = (ViewGroup) rowView;
        TextView placeTv = (TextView) (((ViewGroup) vg.getChildAt(0)).getChildAt(0));
        TextView priceTv = (TextView) (((ViewGroup) vg.getChildAt(0)).getChildAt(1));
        TextView timeLengthTv = (TextView) (((ViewGroup) vg.getChildAt(0)).getChildAt(2));

        String newPlaceTvText = placeTv.getText().toString() + JsonMethods.getString(newMatch, "location");
        placeTv.setText(newPlaceTvText);
        String newPriceTvText = priceTv.getText().toString() + JsonMethods.get(newMatch, "asking_price");
        priceTv.setText(newPriceTvText);
        String newTimeTvText;
        if(readableTimeDifference.contains("d")) {
            newTimeTvText = readableTimeDifference.substring(0, readableTimeDifference.indexOf("d") + 1);
        }
        else {
            newTimeTvText = readableTimeDifference;
        }
        timeLengthTv.setText(newTimeTvText);

        myMatches.add(newMatch);
        Button newB = (Button) (((ViewGroup) vg.getChildAt(0)).getChildAt(3));
        newB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Hunger.this, EditAvPost.class);
                Bundle b = new Bundle();
                b.putString("match_info", myMatches.get(myMatches.size() - 1).toString());
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        });

        final View newRowView = rowView;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout.addView(newRowView);
            }
        });
    }

    private void addAndPostHgPost(JSONObject jsonObject, final int postNum, boolean shouldAdd) {
        //System.out.println(jsonObject.toString());
        if(shouldAdd) {
            myHungerPosts.add(jsonObject);
        }
        LayoutInflater inflator = Hunger.this.getLayoutInflater();
        View rowView = inflator.inflate(R.layout.availability_post, null);
        ViewGroup vg = (ViewGroup) rowView;
        TextView newV = (TextView) (((ViewGroup) vg.getChildAt(0)).getChildAt(0));
        Button newB = (Button) (((ViewGroup) vg.getChildAt(0)).getChildAt(2));
        newB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Hunger.this, EditAvPost.class);
                Bundle b = new Bundle();
                b.putString("screen", "hunger");
                b.putString("post_info", myHungerPosts.get(postNum).toString());
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        });
        String newVText = "Post " + (postNum + 1);
        newV.setText(newVText);
        final View newRowView = rowView;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout.addView(newRowView);
            }
        });
    }

    private void removeAllPosts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < linearLayout.getChildCount(); i++) {
                    linearLayout.removeViewAt(i);
                    i--;
                }
                myHungerPosts = new ArrayList<JSONObject>();
                myMatches = new ArrayList<JSONObject>();
            }
        });
    }

    public void setMatches(final JSONObject filters) {
        Thread get_matches = new Thread() {
            @Override
            public void run() {
                final String currentTime = DateParser.getCurrentDateTimeServer();
                //String size = JsonMethods.getString(filters, "Size");
                String size = "-1/"; // FOR NOW
                String location = JsonMethods.getString(filters, "Location") + "/";
                if(location.equals("/")) location = "false/";
                String maxPrice = JsonMethods.getString(filters, "Price") + "/";
                if(maxPrice.equals("/")) maxPrice = "false/";
                String startTime = JsonMethods.getString(filters, "Start Time") + "/";
                if(startTime.equals("/")) startTime = "false/";
                String endTime = JsonMethods.getString(filters, "End Time") + "/";
                if(endTime.equals("/")) endTime = "false/";
                String username = JsonMethods.getString(filters, "Username") + "/";
                if(username.equals("/")) username = "false/";
                GeneralUtility.setHashIfDoesntExist();
                ServerCommunicationGet scg = new ServerCommunicationGet("availability-list/" + size +
                        location + username + startTime + endTime + maxPrice +
                        GeneralUtility.getAssociatedStringForSort(JsonMethods.getString(filters,"sortby")));
                final Response response = scg.sendGetRequest();

                if(response == null) {
                    return;
                }

                try {
                    //System.out.println(response.code());
                    if(response.code() == 200) {
                        String responseBody = response.body().string();
                        JSONObject matchObj = new JSONObject(responseBody);
                        JSONArray matches = matchObj.getJSONArray("result");
                        //System.out.println(matches.length());
                        for(int i = 0; i < matches.length(); i++) {
                            JSONObject newMatch = matches.getJSONObject(i);
                            addMatch(newMatch, currentTime);
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        get_matches.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(hasBeenStarted) {
            resumed = true;
            if (hungerPostsSet && onPostScreen) {
                removeAllPosts();
                setHungerPosts();
            } else {
                if (newFilters.length() > 0) {
                    removeAllPosts();
                    setMatches(newFilters);
                }
            }
        }
        else {
            hasBeenStarted = true;
        }
    }
}
