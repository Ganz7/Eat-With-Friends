package com.wisconsin.ganz.eatwithfriends;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

public class CreateEvent extends AppCompatActivity {

    private EditText et_location;
    private EditText et_date;
    private EditText et_start_time;
    private EditText et_end_time;
    private EditText et_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViewsAndSetListeners();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processSubmit();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Initialize all the UI Elements and set the Date and Time picker listeners
     * for the relevant elements.
     */
    private void initializeViewsAndSetListeners(){
        et_location = (EditText) findViewById(R.id.et_location);
        et_date = (EditText) findViewById(R.id.et_date);
        et_start_time = (EditText) findViewById(R.id.et_start_time);
        et_end_time = (EditText) findViewById(R.id.et_end_time);
        et_info = (EditText) findViewById(R.id.et_info);

        setTodaysDate();

        setTimePickerListenerHelper(et_start_time);
        setTimePickerListenerHelper(et_end_time);

    }

    /**
     * Sets up TimePicker Dialog and handles onTimeSet events
     * @param et EditText for which time picker dialog needs to be set up
     */
    public void setTimePickerListenerHelper(final EditText et){
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker;
                //Display a 24-hour format TimePickerDialog and set the edit text on selection
                mTimePicker = new TimePickerDialog(CreateEvent.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        et.setText(String.format("%02d", selectedHour) + ":" +
                                String.format("%02d", selectedMinute));
                    }
                }, hour, minute, true);

                mTimePicker.show();
            }
        });
    }

    /**
     * Process user request to create a new event
     */
    private void processSubmit(){

    }

    /**
     * Set the Date Picker EditText to the day's date
     */
    private void setTodaysDate(){
        //TODO
    }
}
