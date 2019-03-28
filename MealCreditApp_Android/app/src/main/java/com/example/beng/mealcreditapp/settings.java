package com.example.beng.mealcreditapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;

import org.json.JSONObject;

import okhttp3.Response;

public class settings extends AppCompatActivity {

    private TextView mTextMessage;

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
        setContentView(R.layout.activity_settings);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        Button btnCancel = (Button) findViewById(R.id.title_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   finish();
               }
           }
        );

        LinearLayout linOfLins = findViewById(R.id.lin_parent);
        for(int i = 0; i < linOfLins.getChildCount() - 1; i++) {
            Button b = (Button) ((LinearLayout) linOfLins.getChildAt(i)).getChildAt(2);
            final TextView valueTextView = (TextView) ((LinearLayout) linOfLins.getChildAt(i)).getChildAt(1);
            final String associatedValue = ((TextView) ((LinearLayout) linOfLins.getChildAt(i)).getChildAt(0)).getText().toString();
            final String basisString = associatedValue.substring(0, associatedValue.indexOf(":"));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText editText = new EditText(settings.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(settings.this);
                    final String lowerBasis = basisString.toLowerCase();
                    editText.setText(valueTextView.getText());
                    builder.setTitle("Change " + basisString)
                        .setMessage(associatedValue)
                        .setView(editText)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String changedValue = editText.getText().toString();
                                if(lowerBasis.equals("email")) {
                                    if(!UserCheck.validate(changedValue)) {
                                        DoAlert.doBasicAlert("Please enter a valid email!", settings.this);
                                        return;
                                    }
                                }
                                //System.out.println("Changed " + basisString + " to " + changedValue);
                                DoAlert.doBasicConfirm(
                                        "Are you sure you'd like to change your " +
                                        lowerBasis + " to: " + changedValue + "?",
                                settings.this,
                                        new Alertable() {
                                            @Override
                                            public void doAction() {
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        JSONObject json = new JSONObject();
                                                        JsonMethods.put(json, lowerBasis, changedValue);
                                                        JsonMethods.put(json, "user_id", User.getUserId());
                                                        JsonMethods.put(json, "token", User.getJwt());
                                                        ServerCommunicationPut scp = new ServerCommunicationPut("change/user/", json.toString());
                                                        final Response response = scp.sendPutRequest();
                                                        //System.out.println("here");
                                                       //System.out.println(response.code());
                                                        if(response == null) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    DoAlert.doBasicAlert("Either our server is down or you're not connected" +
                                                                            " to the internet!", settings.this);
                                                                }
                                                            });
                                                        }
                                                        else if(response.code() == 200) {
                                                            User.updateSingleUserField(lowerBasis, changedValue);
                                                            UserCheck.updateSinglePreference(lowerBasis, changedValue);
                                                            //System.out.println("Changed value to: " + changedValue);
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    valueTextView.setText(changedValue);
                                                                }
                                                            });
                                                        }
                                                    }
                                                }).start();
                                            }
                                        }, null);
                                }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        Button btnLogOut = (Button) findViewById(R.id.logout);
        btnLogOut.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 DoAlert.doBasicConfirm(
                     "Are you sure you'd like to logout?",
                     settings.this,
                     new Alertable() {
                         @Override
                         public void doAction() {
                             UserCheck.resetUserPreferences();
                             Intent intent = new Intent(settings.this, login.class);
                             startActivity(intent);
                             finish();
                         }
                     }, null
                 );
             }}
        );

//        mTextMessage = (TextView) findViewById(R.id.message);
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        TextView tvTemp = findViewById(R.id.firstNameEdit);
        String userFullName = User.getFullName();
        int userSpace = userFullName.indexOf(" ");
        String userUsername = User.getUsername();
        String userEmail = User.getEmail();
        if(!userFullName.equals(" ") && userSpace >= 0) {
            String userFirstName = userFullName.substring(0, userSpace);
            String userLastName = userFullName.substring(userSpace + 1, userFullName.length());
//            userFirstName = "Ben";
//            userLastName = "Glossner";
            tvTemp.setText(userFirstName);
            tvTemp = findViewById(R.id.lastNameEdit);
            tvTemp.setText(userLastName);
        }
        else {
            tvTemp.setText(R.string.nullContent);
            tvTemp = findViewById(R.id.lastNameEdit);
            tvTemp.setText(R.string.nullContent);
        }

        tvTemp = findViewById(R.id.usernameEdit);
        if(!userUsername.equals("")) {
            tvTemp.setText(userUsername);
        }
        else {
            tvTemp.setText(R.string.nullContent);
        }

        tvTemp = findViewById(R.id.emailEdit);
        if(!userEmail.equals("")) {
            tvTemp.setText(userEmail);
        }
        else {
            tvTemp.setText(R.string.nullContent);
        }
    }
}
