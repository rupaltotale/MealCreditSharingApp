package com.example.beng.mealcreditapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.spec.ECField;
import java.util.Calendar;

import okhttp3.Response;

public class EditAvPost extends AppCompatActivity {

    private TextView tv_date1, tv_date2;
    private TextView tv_time1, tv_time2;
    private DatePickerDialog.OnDateSetListener mDateSetListener1, mDateSetListener2;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener1, mTimeSetListener2;
    private DateParser dateParser = new DateParser();
    private boolean isAvEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_av_post);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.edit_post_title);
        actionBar.setElevation(0);

        Bundle b = getIntent().getExtras();
        String postInfo = ""; // or other values
        if(b != null) {
            if("hunger".equals(b.getString("screen"))) {
                isAvEdit = false;
            }
            else {
                isAvEdit = true;
                postInfo = b.getString("post_info");
                setUpPage(postInfo);
            }
        }
        else {
            /*Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);*/
            finish();
        }
    }

    private void setUpPage(String info) {
        System.out.println(info);
        final JSONObject jsonPostInfo = JsonMethods.convertToJSONFromString(info);
        final String compareValue = JsonMethods.getString(jsonPostInfo, "location");
        final Spinner locList = (Spinner) findViewById(R.id.location_list_edit);
        int locationListToUse = isAvEdit ? R.array.locations : R.array.locations2;
        ArrayAdapter<String> locAdapter = new ArrayAdapter<String>(EditAvPost.this,
                R.layout.location_spinner_item, getResources().getStringArray(locationListToUse));
        locAdapter.setDropDownViewResource((android.R.layout.simple_spinner_dropdown_item));
        locList.setAdapter(locAdapter);
        int spinnerPosition = locAdapter.getPosition(compareValue);
        locList.setSelection(spinnerPosition);

        Button savePost = (Button) findViewById(R.id.av_save_edit);
        savePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String price = ((TextView) findViewById(R.id.edit_price_edit)).getText().toString();
                String startDate = ((TextView) findViewById(R.id.datepick1_edit)).getText().toString();
                String endDate = ((TextView) findViewById(R.id.datepick2_edit)).getText().toString();
                String startTime = ((TextView) findViewById(R.id.timepick1_edit)).getText().toString();
                String endTime = ((TextView) findViewById(R.id.timepick2_edit)).getText().toString();
                final String formattedStartDate = DateParser.convertSlashDateTime(startDate, startTime);
                final String formattedEndDate = DateParser.convertSlashDateTime(endDate, endTime);
                final String location = locList.getSelectedItem().toString();
                final String associatedId = isAvEdit ? JsonMethods.get(jsonPostInfo, "av_id") : JsonMethods.get(jsonPostInfo, "hg_id");
                JSONObject json = new JSONObject();
                try {
                    if(isAvEdit) {
                        json.put("asking_price", price);
                        json.put("av_id", associatedId);
                    }
                    else {
                        json.put("max_price", price);
                        json.put("hg_id", associatedId);
                    }
                    json.put("location", location); json.put("user_id", User.getUserId());
                    json.put("start_time", formattedStartDate); json.put("end_time", formattedEndDate); json.put("token", User.getJwt());
                } catch (JSONException e) {}

                final String jsonText = json.toString();
                System.out.println("Formatted Start: " + formattedStartDate + "\n" + "Formatted End: " + formattedEndDate);
                Thread newRequestThread = new Thread() {
                    @Override
                    public void run() {
                        String serverUrl = isAvEdit ? "change/availability/" : "change/hunger/";
                        ServerCommunicationPut scp = new ServerCommunicationPut(serverUrl, jsonText);
                        Response response = scp.sendPutRequest();
                        try {
                            //System.out.println(response.code());
                            if(response.code() == 200) {
                                //MainActivity.changeAvPost(avId, new JSONObject(jsonText));
                                finish();
                            }
                        }
                        catch (Exception e) {}
                    }
                };
                newRequestThread.start();
                //finish();
            }
        });

        Button deletePost = (Button) findViewById(R.id.av_delete_edit);
        deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                try {
                    if(isAvEdit) {
                        json.put("av_id", JsonMethods.get(jsonPostInfo, "av_id"));
                    } else {
                        json.put("hg_id", JsonMethods.get(jsonPostInfo, "hg_id"));
                    }
                    json.put("user_id", User.getUserId()); json.put("token", User.getJwt());
                } catch (JSONException e) { return; }
                final String jsonText = JsonMethods.convertJSONToString(json);
                Thread newRequestThread = new Thread() {
                    @Override
                    public void run() {
                        String serverUrl = isAvEdit ? "delete/availability/" : "delete/hunger/";
                        ServerCommunicationDelete scp = new ServerCommunicationDelete(serverUrl, jsonText);
                        Response response = scp.sendDeleteRequest();
                        try {
                            System.out.println(response.code());
                            if(response.code() == 200) {
                                //MainActivity.changeAvPost(avId, new JSONObject(jsonText));
                                finish();
                            }
                        }
                        catch (Exception e) {}
                    }
                };
                newRequestThread.start();
            }
        });


        TextView tvPrice = (TextView) findViewById(R.id.edit_price_edit);
        tvPrice.setText(JsonMethods.getString(jsonPostInfo, "asking_price"));
        String startDateTime = DateParser.reverseParseServerDateTime(JsonMethods.getString(jsonPostInfo, "start_time"));
        String endDateTime = DateParser.reverseParseServerDateTime(JsonMethods.getString(jsonPostInfo, "end_time"));
        if(startDateTime.equals("null") || endDateTime.equals("null")) {
            finish();
        }

        int startDividerIndex = startDateTime.indexOf("|");
        int endDividerIndex = endDateTime.indexOf("|");
        Calendar cal = Calendar.getInstance();
        tv_date1 = (TextView) findViewById(R.id.datepick1_edit);
        tv_date2 = (TextView) findViewById(R.id.datepick2_edit);
        String startDate = startDateTime.substring(0, startDividerIndex);
        String endDate = endDateTime.substring(0, endDividerIndex);
        tv_date1.setText(startDate);
        tv_date2.setText(endDate);
        tv_time1 = (TextView) findViewById(R.id.timepick1_edit);
        tv_time2 = (TextView) findViewById(R.id.timepick2_edit);
        String startTime = startDateTime.substring(startDividerIndex + 1);
        String endTime = endDateTime.substring(endDividerIndex + 1);
        tv_time1.setText(startTime);
        tv_time2.setText(endTime);

        tv_date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        EditAvPost.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener1,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                //Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);
                String date = month + "/" + day + "/" + year;
                tv_date1.setText(date);
            }
        };
        tv_date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        EditAvPost.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener2,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                //Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);
                String date = month + "/" + day + "/" + year;
                tv_date2.setText(date);
            }
        };

        tv_time1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int minute = cal.get(Calendar.MINUTE);
                int h = cal.get(Calendar.HOUR_OF_DAY);

                TimePickerDialog dialog = new TimePickerDialog(
                        EditAvPost.this, mTimeSetListener1, h, minute, false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mTimeSetListener1 = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String relation = hour >= 12 ? "PM" : "AM";
                String minuteStr = minute < 10 ? "0" + minute : Integer.toString(minute);
                String newStr = DateParser.getAppropriateHourFrom24(relation, hour) + ":" + minuteStr + " " + relation;
                tv_time1.setText(newStr);
            }
        };

        tv_time2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int minute = cal.get(Calendar.MINUTE);
                int h = cal.get(Calendar.HOUR_OF_DAY);

                TimePickerDialog dialog = new TimePickerDialog(
                        EditAvPost.this, mTimeSetListener2, h, minute, false);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mTimeSetListener2 = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String relation = hour >= 12 ? "PM" : "AM";
                String minuteStr = minute < 10 ? "0" + minute : Integer.toString(minute);
                String newStr = DateParser.getAppropriateHourFrom24(relation, hour) + ":" + minuteStr + " " + relation;
                tv_time2.setText(newStr);
            }
        };
    }
}
