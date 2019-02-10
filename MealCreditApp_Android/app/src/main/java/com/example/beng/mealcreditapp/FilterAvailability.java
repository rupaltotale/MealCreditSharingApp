package com.example.beng.mealcreditapp;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Date;

public class FilterAvailability extends AppCompatActivity {

    private int currentSelection;
    private int lastSelected;
    private int[] buttonIds;
    private LinearLayout mainLayout;
    private int lastLength;

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
            R.id.filter_other
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

        setUpLocationPage();
        setUpPricePage();
        setUpTimePage();
        setUpTimePage();

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
            /*case R.id.filter_username:
                setUpUsernamePage();
                break;*/
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

    private void setUpTimePage() {
        LayoutInflater inflator = FilterAvailability.this.getLayoutInflater();
        View rowView = inflator.inflate(R.layout.time_page_filter, null);
        final EditText et = rowView.findViewById(R.id.date_edit_filter);
        final String currentDate = DateParser.getCurrentDateTime();
        final String newText = currentDate.substring(0, currentDate.indexOf("M") - 2);
        final TextView tvRelation = rowView.findViewById(R.id.relation_time_filter);
        final Switch relationSwitch = rowView.findViewById(R.id.ampmtoggle);
        if(currentDate.contains("PM")) {
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
                //System.out.println(s + " " + start + " " + after);
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
                if(currentDate.contains("PM")) {
                    tvRelation.setText(R.string.pmStr);
                    relationSwitch.setChecked(true);
                }
            }
        });

        final View newRowView = rowView;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainLayout.addView(newRowView);
            }
        });
    }

    private int getLength(EditText e) {
        return e.getText().toString().length();
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
                return 5;
            default:
                return -1;
        }
    }
}
