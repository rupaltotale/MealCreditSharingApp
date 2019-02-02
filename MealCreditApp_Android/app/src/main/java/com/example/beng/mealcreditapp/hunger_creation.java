package com.example.beng.mealcreditapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.widget.DatePicker;
import android.widget.TimePicker;
import android.app.TimePickerDialog;
import android.util.Log;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;


public class hunger_creation extends AppCompatActivity {

    private TextView tv_date1, tv_date2;
    private TextView tv_time1, tv_time2;
    private DatePickerDialog.OnDateSetListener mDateSetListener1, mDateSetListener2;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener1, mTimeSetListener2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunger_creation);

        Button exitPopup = (Button) findViewById(R.id.popup_exit_hg);
        exitPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Spinner locList = (Spinner) findViewById(R.id.location_list_hg);
        ArrayAdapter<String> locAdapter = new ArrayAdapter<String>(hunger_creation.this,
                R.layout.location_spinner_item, getResources().getStringArray(R.array.locations2));
        locAdapter.setDropDownViewResource((android.R.layout.simple_spinner_dropdown_item));
        locList.setAdapter(locAdapter);

        Button savePost = (Button) findViewById(R.id.hg_save);
        savePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String price = ((TextView) findViewById(R.id.edit_text_hg)).getText().toString();
                String startDate = ((TextView) findViewById(R.id.datepick1_hg)).getText().toString();
                String endDate = ((TextView) findViewById(R.id.datepick2_hg)).getText().toString();
                String startTime = ((TextView) findViewById(R.id.timepick1_hg)).getText().toString();
                String endTime = ((TextView) findViewById(R.id.timepick2_hg)).getText().toString();
                final String formattedStartDate = DateParser.convertSlashDateTime(startDate, startTime);
                final String formattedEndDate = DateParser.convertSlashDateTime(endDate, endTime);
                final String location = locList.getSelectedItem().toString();
                JSONObject json = new JSONObject();
                try {
                    json.put("max_price", price); json.put("user_id", User.getUserId()); json.put("location", location);
                    json.put("start_time", formattedStartDate); json.put("end_time", formattedEndDate); json.put("token", User.getJwt());
                } catch (JSONException e) {}

                final String jsonText = json.toString();
                Thread newRequestThread = new Thread() {
                    @Override
                    public void run() {
                        ServerCommunicationPost scp = new ServerCommunicationPost("create/hunger/", jsonText);
                        Response response = scp.sendPostRequest();
                        try {
                            System.out.println(response.code());
                            if(response.code() == 200) {
                                finish();
                            }
                        }
                        catch (Exception e) {}
                    }
                };
                newRequestThread.start();
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * .8), (int) (height * .8));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);

        Calendar cal = Calendar.getInstance();
        tv_date1 = (TextView) findViewById(R.id.datepick1_hg);
        tv_date2 = (TextView) findViewById(R.id.datepick2_hg);
        String datePick = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);
        tv_date1.setText(datePick);
        tv_date2.setText(datePick);
        tv_time1 = (TextView) findViewById(R.id.timepick1_hg);
        tv_time2 = (TextView) findViewById(R.id.timepick2_hg);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        String relation = hour >= 12 ? "PM" : "AM";
        String minuteStr = min < 10 ? "0" + min : Integer.toString(min);
        //System.out.println("Current hour:" + hour);
        String timePick = DateParser.getAppropriateHourFrom24(relation, hour) + ":" + minuteStr + " " + relation;
        tv_time1.setText(timePick);
        tv_time2.setText(timePick);

        tv_date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        hunger_creation.this,
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
                        hunger_creation.this,
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
                        hunger_creation.this, mTimeSetListener1, h, minute, false);
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
                System.out.println("NEW TIME: " + newStr);
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
                        hunger_creation.this, mTimeSetListener2, h, minute, false);
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
