package com.example.beng.mealcreditapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    public static ArrayList<JSONObject> myAvailabilities = new ArrayList<JSONObject>();
    LinearLayout linearLayout;
    //LinearLayout.LayoutParams lp;
    int width, height;
    int totalPosts;
    private boolean availabilitiesSet = false;
    private static ArrayList<Integer> needUpdateAv = new ArrayList<Integer>();
    private static ArrayList<Integer> needRemovalAv = new ArrayList<Integer>();

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
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title_hg);
        //System.out.println("HERE...");
        //System.out.println(User.getAllInfoAsJSONString());

        mTextMessage = (TextView) findViewById(R.id.message);
        //BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Button btnSettings = (Button) findViewById(R.id.title_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(MainActivity.this, settings.class);
                   startActivity(intent);
                   //setAvailabilities();
               }
           }
        );

        Button addAvailability = (Button) findViewById(R.id.add_hunger);
        addAvailability.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(MainActivity.this, availability_dialog.class);
                   startActivity(intent);
               }
           }
        );

        linearLayout = findViewById(R.id.hg_layout);
        setAvailabilities();
    }

    private void setAvailabilities() {
        Thread av_thread = new Thread() {
            @Override
            public void run() {
                if(UserCheck.hasInitialized()) {
                    String userId = User.getUserId();
                    //System.out.println("User id: " + userId);
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
                                totalPosts = amntOfPosts;
                                for(int i = 0; i < amntOfPosts; i++) {
                                    //System.out.println(jsonArray.getJSONObject(i).toString());
                                    JSONObject newAvPost = jsonArray.getJSONObject(i);
                                    /*if(!availabilitiesSet || (i < myAvailabilities.size() &&
                                            !(JsonMethods.convertJSONToString(myAvailabilities.get(i)).equals(JsonMethods.convertJSONToString(newAvPost))))) {
                                        addAndPostAvPost(newAvPost, i, true);
                                    }*/
                                    addAndPostAvPost(newAvPost, i, true);
                                }
                                availabilitiesSet = true;
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

    private void addAndPostAvPost(JSONObject jsonObject, final int postNum, boolean shouldAdd) {
        //System.out.println(jsonObject.toString());
        if(shouldAdd) {
            myAvailabilities.add(jsonObject);
        }
        LayoutInflater inflator = MainActivity.this.getLayoutInflater();
        View rowView = inflator.inflate(R.layout.availability_post, null);
        ViewGroup vg = (ViewGroup) rowView;
        TextView newV = (TextView) (((ViewGroup) vg.getChildAt(0)).getChildAt(0));
        Button newB = (Button) (((ViewGroup) vg.getChildAt(0)).getChildAt(2));
        newB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditAvPost.class);
                Bundle b = new Bundle();
                b.putString("post_info", myAvailabilities.get(postNum).toString());
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        });
        String newVText = "POST " + (postNum + 1);
        newV.setText(newVText);
        final View newRowView = rowView;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout.addView(newRowView);
            }
        });
    }

    private void removeAll() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < linearLayout.getChildCount(); i++) {
                    linearLayout.removeViewAt(i);
                    myAvailabilities = new ArrayList<JSONObject>();
                    i--;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        removeAll();
        if(availabilitiesSet) {
            setAvailabilities();
        }
    }

    /*private void removePostView(final int childIndex) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                linearLayout.removeViewAt(childIndex);
            }
        });
    }

    /*private void setParams() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
    }*/

    /*public static ArrayList<JSONObject> getAvPosts() {
        return myAvailabilities;
    }

    public static boolean changeAvPost(String avId, JSONObject newAvPost) {
        for(int i = 0; i < myAvailabilities.size(); i++) {
            if(JsonMethods.get(myAvailabilities.get(i), "av_id").equals(avId)) {
                myAvailabilities.set(i, newAvPost);
                needUpdateAv.add(i);
                return true;
            }
        }

        return false;
    }

    public static void addAvPost(JSONObject avPost) {
        myAvailabilities.add(avPost);
        needUpdateAv.add(myAvailabilities.size() - 1);
    }

    public static boolean removeAvPost(String avId) {
        for(int i = 0; i < myAvailabilities.size(); i++) {
            if(JsonMethods.get(myAvailabilities.get(i), "av_id").equals(avId)) {
                myAvailabilities.remove(i);
                needRemovalAv.add(i);
                return true;
            }
        }

        return false;
    }

    private void doUIRemoval() {
        for(int i = 0; i < needRemovalAv.size(); i++) {
            removePostView(needRemovalAv.get(i));
            i--;
        }
    }

    private void doUIAddition() {
        for(int i = 0; i < needUpdateAv.size(); i++) {

        }
    }*/
}
