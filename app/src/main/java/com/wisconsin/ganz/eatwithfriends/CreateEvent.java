package com.wisconsin.ganz.eatwithfriends;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
                extractDataAndSubmit();
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

        EditTextDatePicker startDatePicker = new EditTextDatePicker(this, R.id.et_date);

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
    private void extractDataAndSubmit(){
        String location = et_location.getText().toString().trim();
        String date = et_date.getText().toString().trim();
        String start_time = et_start_time.getText().toString().trim();
        String end_time = et_end_time.getText().toString().trim();
        String info = et_info.getText().toString().trim();

        // Check if the fields are filled in
        // TODO: Make toast notifications individually
        if(location.length() == 0 || date.length() == 0 || start_time.length() == 0 || end_time.length() == 0){
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_LONG).show();
            return;
        }

        String startDateString = et_date + " " + et_start_time;
        String endDateString = et_date + " " + et_end_time;

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm"); //Custom time format

        Date startDate = null;
        Date endDate = null;
        try {
            startDate = format.parse(startDateString);
            endDate = format.parse(endDateString);
        } catch (ParseException e) {
            Log.e("Parse Exception", "Error : "+e.getMessage());
        }

        long startMillis = startDate.getTime();
        long endMillis = endDate.getTime();

        //If the event is in reverse chronological order, do not add
        if(startMillis > endMillis){
            Toast.makeText(this, "Start time cannot be later than end time", Toast.LENGTH_LONG).show();
            return;
        }

        addEvent(location, date, start_time, end_time, info);
    }

    /**
     *
     */
    private void addEvent(String location, String date, String start_time, String end_time, String info){

    }

    /**
     * Set the Date Picker EditText to the day's date
     */
    private void setTodaysDate(){
        //TODO
    }
}
