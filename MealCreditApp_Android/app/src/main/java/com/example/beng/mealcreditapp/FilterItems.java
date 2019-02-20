package com.example.beng.mealcreditapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class FilterItems extends AppCompatActivity {

    private String filterItems;
    private JSONObject filterItemsJSON;
    private LinearLayout filterLayout;
    //private ArrayList<String> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_items);

        filterLayout = findViewById(R.id.filter_dialog_lin_layout);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            filterItems = b.getString("filter_info");
            filterItemsJSON = JsonMethods.convertToJSONFromString(filterItems);
            setUpFilterItems();
        }
        else {
            finish();
        }

        final Spinner locList = findViewById(R.id.sort_location_filter);
        ArrayAdapter<String> locAdapter = new ArrayAdapter<String>(FilterItems.this,
                R.layout.sort_spinner_item, getResources().getStringArray(R.array.sortByOptions));
        locList.setAdapter(locAdapter);

        Button exitPopup = (Button) findViewById(R.id.popup_exit_filter);
        exitPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button filterBut = findViewById(R.id.filter_items_but);
        filterBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isGood = doChecks();
                if(!isGood) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DoAlert.doBasicAlert("Error in some field", FilterItems.this);
                        }
                    });
                }
                else {
                    JsonMethods.put(filterItemsJSON, "sortby", ((Spinner) findViewById(R.id.sort_location_filter)).getSelectedItem().toString());
                    FilterAvailability.filtersToUse = filterItemsJSON;
                    finish();
                }
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
    }

    private void setUpFilterItems() {
        final Iterator<String> keys = filterItemsJSON.keys();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                while(keys.hasNext()) {
                    LayoutInflater inflater = FilterItems.this.getLayoutInflater();
                    final View rowView = inflater.inflate(R.layout.exit_filter_layout, null);
                    final String newKey = keys.next();
                    TextView newTv = rowView.findViewById(R.id.filter_label);
                    //newKey = "- " + newKey;
                    newTv.setText(newKey);
                    Button b = rowView.findViewById(R.id.exit_filter_but);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            filterLayout.removeView(rowView);
                            if(filterLayout.getChildCount() == 0) {
                                filterItemsJSON.remove(newKey);
                                finish();
                            }
                        }
                    });
                    filterLayout.addView(rowView);
                }
            }
        });
    }

    private boolean doChecks() {
        if(filterItemsJSON.has("Start Time")) {
            if(!DateParser.isAcceptableFilterTime(JsonMethods.getString(filterItemsJSON, "Start Time"))) {
                return false;
            }
        }
        if(filterItemsJSON.has("End Time")) {
            if(!DateParser.isAcceptableFilterTime(JsonMethods.getString(filterItemsJSON, "End Time"))) {
                return false;
            }
        }
        if(filterItemsJSON.has("Price")) {
            if(!GeneralUtility.checkPrice(JsonMethods.getString(filterItemsJSON, "Price"))) {
                return false;
            }
        }

        return true;
    }
}
