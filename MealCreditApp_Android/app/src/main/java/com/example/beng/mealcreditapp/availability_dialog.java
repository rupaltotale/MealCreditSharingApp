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


public class availability_dialog extends AppCompatActivity {

    private TextView tv_date1, tv_date2;
    private TextView tv_time1, tv_time2;
    private DatePickerDialog.OnDateSetListener mDateSetListener1, mDateSetListener2;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener1, mTimeSetListener2;
    private static final String TAG = "activity_dialog";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availability_dialog);

        Button exitPopup = (Button) findViewById(R.id.popup_exit);
        exitPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Spinner locList = (Spinner) findViewById(R.id.location_list);
        ArrayAdapter<String> locAdapter = new ArrayAdapter<String>(availability_dialog.this,
                R.layout.location_spinner_item, getResources().getStringArray(R.array.locations));
        locAdapter.setDropDownViewResource((android.R.layout.simple_spinner_dropdown_item));
        locList.setAdapter(locAdapter);

        Button savePost = (Button) findViewById(R.id.av_save);
        savePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String price = ((TextView) findViewById(R.id.edit_text)).getText().toString();
                String startDate = ((TextView) findViewById(R.id.datepick1)).getText().toString();
                String endDate = ((TextView) findViewById(R.id.datepick2)).getText().toString();
                String startTime = ((TextView) findViewById(R.id.timepick1)).getText().toString();
                String endTime = ((TextView) findViewById(R.id.timepick2)).getText().toString();
                final String formattedStartDate = DateParser.convertSlashDateTime(startDate, startTime);
                final String formattedEndDate = DateParser.convertSlashDateTime(endDate, endTime);
                final String location = locList.getSelectedItem().toString();
                JSONObject json = new JSONObject();
                try {
                    json.put("asking_price", price); json.put("location", location); json.put("user_id", User.getUserId());
                    json.put("start_time", formattedStartDate); json.put("end_time", formattedEndDate); json.put("token", User.getJwt());
                } catch (JSONException e) {}

                final String jsonText = json.toString();
                //System.out.println("Formatted Start: " + formattedStartDate + "\n" + "Formatted End: " + formattedEndDate);
                Thread newRequestThread = new Thread() {
                    @Override
                    public void run() {
                        ServerCommunicationPost scp = new ServerCommunicationPost("create/availability/", jsonText);
                        Response response = scp.sendPostRequest();
                        try {
                            if(response.code() == 200) {
                                /*System.out.println("Returned body: " + response.body().string());
                                String avId = new JSONObject(response.body().string()).getString("add_info");
                                JSONObject convertedBack = new JSONObject(jsonText);
                                convertedBack.put("av_id", avId);
                                MainActivity.addAvPost(convertedBack);*/
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
        tv_date1 = (TextView) findViewById(R.id.datepick1);
        tv_date2 = (TextView) findViewById(R.id.datepick2);
        String datePick = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR);
        tv_date1.setText(datePick);
        tv_date2.setText(datePick);
        tv_time1 = (TextView) findViewById(R.id.timepick1);
        tv_time2 = (TextView) findViewById(R.id.timepick2);
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
                        availability_dialog.this,
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
                        availability_dialog.this,
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
                        availability_dialog.this, mTimeSetListener1, h, minute, false);
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
                        availability_dialog.this, mTimeSetListener2, h, minute, false);
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
