package com.example.beng.mealcreditapp;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class FilterAvailability extends AppCompatActivity {

    private int currentSelection;
    private int lastSelected;
    private int[] buttonIds;
    private String[] associatedStrings;
    private LinearLayout mainLayout;
    private String initialDateTime;
    private int lastLength;
    private ArrayList<String> hasBeenEdited = new ArrayList<String>();
    private String recordedCurrentDate;
    public static JSONObject filtersToUse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_availability);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.filter_activity_name);
        actionBar.setElevation(0);

        buttonIds = new int[] {
            R.id.filter_location,
            R.id.filter_price,
            R.id.filter_start_time,
            R.id.filter_end_time,
            R.id.filter_username,
            R.id.filter_other,
            R.id.filter_filter
        };
        associatedStrings = new String[] {
            "Location",
            "Price",
            "Start Time",
            "End Time",
            "Username",
            "Other"
        };
        mainLayout = findViewById(R.id.lin_layout_filter_av);
        lastSelected = 0;

        for(final int i : buttonIds) {
            final Button curBut = findViewById(i);
            curBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setUnderline(i, true);
                    setUpPage(i);
                }
            });
        }

        recordedCurrentDate = DateParser.getCurrentDateTime();

        setUpLocationPage();
        setUpPricePage();
        setUpTimePage(0);
        setUpTimePage(1);
        setUpUsernamePage();
        setUpFilterPage();

        currentSelection = R.id.filter_location;
        setUnderline(currentSelection, false);
        setVisiblePage(0);
    }

    private void setUnderline(int newSelection, boolean wasOldSelection) {
        lastSelected = currentSelection;
        currentSelection = newSelection;
        Button currButSelected = findViewById(currentSelection);
        if(GeneralUtility.version < 16) {
            if(wasOldSelection) {
                findViewById(lastSelected).setBackgroundDrawable(GeneralUtility.getDrawable(this, R.drawable.border_top));
            }
            currButSelected.setBackgroundDrawable(GeneralUtility.getDrawable(this, R.drawable.border_top_bot));
        } else {
            if(wasOldSelection) {
                findViewById(lastSelected).setBackground(GeneralUtility.getDrawable(this, R.drawable.border_top));
            }
            currButSelected.setBackground(GeneralUtility.getDrawable(this, R.drawable.border_top_bot));
        }
    }

    private void hidePage() {
        int i = getViewNumFromId(lastSelected);
        if(i != -1) {
            View v = mainLayout.getChildAt(i);
            v.setVisibility(LinearLayout.GONE);
        }
    }

    private void setUpPage(int selectionId) {
        hidePage();
        switch(selectionId) {
            case R.id.filter_location:
                setVisiblePage(0);
                break;
            case R.id.filter_price:
                setVisiblePage(1);
                break;
            case R.id.filter_start_time:
                setVisiblePage(2);
                break;
            case R.id.filter_end_time:
                setVisiblePage(3);
                break;
            case R.id.filter_username:
                setVisiblePage(4);
                break;
            case R.id.filter_filter:
                setVisiblePage(5); //6 cuz of below
                break;
            default:
                //setUpOtherPage();
                break;
        }
    }

    private void setVisiblePage(int num) {
        View v = mainLayout.getChildAt(num);
        v.setVisibility(LinearLayout.VISIBLE);
    }

    private void setUpLocationPage() {
        LayoutInflater inflator = FilterAvailability.this.getLayoutInflater();
        View rowView = inflator.inflate(R.layout.location_page_filter, null);
        final Spinner locList = rowView.findViewById(R.id.location_list_filter);
        ArrayAdapter<String> locAdapter = new ArrayAdapter<String>(FilterAvailability.this,
                R.layout.location_spinner_item_filter, getResources().getStringArray(R.array.locations2));
        locList.setAdapter(locAdapter);
        final View newRowView = rowView;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainLayout.addView(newRowView);
            }
        });
    }

    private void setUpPricePage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflator = FilterAvailability.this.getLayoutInflater();
                View rowView = inflator.inflate(R.layout.price_page_filter, null);
                mainLayout.addView(rowView);
            }
        });
    }

    private void setUpTimePage(int which) {
        final String whichStr = which == 0 ? "Start Time" : "End Time";
        LayoutInflater inflator = FilterAvailability.this.getLayoutInflater();
        View rowView = inflator.inflate(R.layout.time_page_filter, null);
        final EditText et = rowView.findViewById(R.id.date_edit_filter);
        final String newText = recordedCurrentDate.substring(0, recordedCurrentDate.indexOf("M") - 2);
        final TextView tvRelation = rowView.findViewById(R.id.relation_time_filter);
        final Switch relationSwitch = rowView.findViewById(R.id.ampmtoggle);
        if(recordedCurrentDate.contains("PM")) {
            tvRelation.setText(R.string.pmStr);
            relationSwitch.setChecked(true);
        }
        relationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvRelation.setText(R.string.pmStr);
                } else {
                    tvRelation.setText(R.string.amStr);
                }
            }
        });
        et.setText(newText);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(after == 0) {
                    char toDelete = s.charAt(start);
                    switch(toDelete) {
                        case '/':
                        case ':':
                        case ' ':
                            setEditText(s, et, start);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*int currLength = getLength(et);
                if(currLength < lastLength) {
                    switch(lastLength) {
                        case 4:
                        case 7:
                        case 12:
                        case 15:
                            deleteFromEditText(et);
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (currLength) {
                        case 2:
                        case 5:
                            addToEditText(et, "/");
                            break;
                        case 10:/
                            addToEditText(et, " ");
                            break;
                        case 13:
                            addToEditText(et, ":");
                            break;
                        case 16:
                            et.clearFocus();
                            break;
                        default:
                            break;
                    }
                }
                lastLength = currLength;*/
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Button resetBut = rowView.findViewById(R.id.reset_time_filter);
        resetBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et.setText(newText);
                if(recordedCurrentDate.contains("PM")) {
                    tvRelation.setText(R.string.pmStr);
                    relationSwitch.setChecked(true);
                }
            }
        });

        final View newRowView = rowView;
        newRowView.setId(30 + which);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainLayout.addView(newRowView);
            }
        });
    }

    private void setUpUsernamePage() {
        LayoutInflater inflater = FilterAvailability.this.getLayoutInflater();
        final View rowView = inflater.inflate(R.layout.username_page_filter, null);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainLayout.addView(rowView);
            }
        });
    }

    private void setUpFilterPage() {
        LayoutInflater inflater = FilterAvailability.this.getLayoutInflater();
        final View rowView = inflater.inflate(R.layout.filter_page_filter, null);
        final Button filterBut = rowView.findViewById(R.id.filter_but_filter);
        filterBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                JSONObject json = new JSONObject();
                String username =  ((EditText) findViewById(R.id.username_edit_filter)).getText().toString();
                if(!username.equals("")) {
                    JsonMethods.put(json, "Username", username);
                }
                if(!recordedCurrentDate.equals(getDateTimeFromTimePage(0))) {
                    JsonMethods.put(json, "Start Time", getDateTimeFromTimePage(0));
                }
                if(!recordedCurrentDate.equals(getDateTimeFromTimePage(1))) {
                    JsonMethods.put(json, "End Time", getDateTimeFromTimePage(1));
                }
                String price = ((EditText) findViewById(R.id.price_page_filter)).getText().toString();
                if(!price.equals("")) {
                    JsonMethods.put(json, "Price", price);
                }
                String location = ((Spinner) findViewById(R.id.location_list_filter)).getSelectedItem().toString();
                if(!location.equalsIgnoreCase("anywhere")) {
                    JsonMethods.put(json, "Location", location);
                }

                if(json.length() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DoAlert.doBasicAlert("You must enter something to filter!", FilterAvailability.this);
                        }
                    });
                    return;
                }

                b.putString("filter_info", JsonMethods.convertJSONToString(json));
                Intent intent = new Intent(FilterAvailability.this, FilterItems.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainLayout.addView(rowView);
            }
        });
    }

    private int getLength(EditText e) {
        return e.getText().toString().length();
    }

    private void addIfNotInEdited(String s) {
        if(!hasBeenEdited.contains(s)) {
            hasBeenEdited.add(s);
        }
    }

    private void removeIfExistsInEdit(String s) {
        hasBeenEdited.remove(s);
    }

    private void addToEditText(CharSequence cs, EditText e, String s, int index) {
        String newStr = cs + s;
        e.setText(newStr);
        e.setSelection(index + 1);
    }

    private void setEditText(CharSequence cs, EditText e, int index) {
        e.setText(cs);
        e.setSelection(index + 1);
    }

    private void deleteFromEditText(EditText e) {
        String s = e.getText().toString();
        if(s.length() > 0) {
            String s1 = s.substring(0, s.length() - 1);
            e.setText(s1);
            e.setSelection(s1.length());
        }
    }

    private int getViewNumFromId(int id) {
        switch (id) {
            case R.id.filter_location:
                return 0;
            case R.id.filter_price:
                return 1;
            case R.id.filter_start_time:
                return 2;
            case R.id.filter_end_time:
                return 3;
            case R.id.filter_username:
                return 4;
            case R.id.filter_other:
                return 6;
            case R.id.filter_filter:
                return 5;
            default:
                return -1;
        }
    }

    private String getDateTimeFromTimePage(int which) {
        View view = findViewById(30 + which);
        EditText et = view.findViewById(R.id.date_edit_filter);
        String tv = ((TextView) view.findViewById(R.id.relation_time_filter)).getText().toString();

        return et.getText().toString() + " " + tv;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(filtersToUse != null && filtersToUse.length() > 0) {
            Hunger.newFilters = filtersToUse;
            filtersToUse = new JSONObject();
            finish();
        }
    }
}
