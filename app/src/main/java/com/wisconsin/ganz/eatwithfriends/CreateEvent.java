package com.wisconsin.ganz.eatwithfriends;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class CreateEvent extends AppCompatActivity {

    //private EditText et_location;
    private Button bu_location;
    private Button et_date;
    private Button et_start_time;
    private Button et_end_time;
    private EditText et_info;

    // Keep Track of Server Add Task
    private ServerAddTask mAddTask = null;

    // ID for ACCESS_FINE_LOCATION permission request
    private static int REQUEST_ACCESS_FINE_LOCATION = 0;

    // ID for Place Picker Activity Request
    private static int PLACE_PICKER_REQUEST = 1;

    // For progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViewsAndSetListeners();
        requestFineLocationAccess();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                extractDataAndSubmit();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private boolean requestFineLocationAccess() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setUpPlacePicker();
            return true;
        }
        /*
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            Snackbar.make(et_location, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                        }
                    });
        } else {*/
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        //}
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Do regular stuff here
                setUpPlacePicker();
            }
            else{
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    private void setUpPlacePicker(){
        //et_location.setFocusable(false);
        bu_location.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(CreateEvent.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String placeInfo = String.format("%s | %s", place.getName(), place.getAddress());
                bu_location.setText(placeInfo);
            }
        }
    }

    /**
     * Initialize all the UI Elements and set the Date and Time picker listeners
     * for the relevant elements.
     */
    private void initializeViewsAndSetListeners(){
        //et_location = (EditText) findViewById(R.id.et_location);
        bu_location = (Button) findViewById(R.id.et_location);
        et_date = (Button) findViewById(R.id.et_date);
        et_start_time = (Button) findViewById(R.id.et_start_time);
        et_end_time = (Button) findViewById(R.id.et_end_time);
        et_info = (EditText) findViewById(R.id.et_info);

        // Set up the Progress Dialog object
        progressDialog = new ProgressDialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Adding Event");

        setTodaysDate();

        setTimePickerListenerHelper(et_start_time);
        setTimePickerListenerHelper(et_end_time);

        EditTextDatePicker startDatePicker = new EditTextDatePicker(this, R.id.et_date);

    }

    /**
     * Sets up TimePicker Dialog and handles onTimeSet events
     * @param et EditText for which time picker dialog needs to be set up
     */
    public void setTimePickerListenerHelper(final Button et){
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
        String location = bu_location.getText().toString().trim();
        String date = et_date.getText().toString().trim();
        String start_time = et_start_time.getText().toString().trim();
        String end_time = et_end_time.getText().toString().trim();
        String info = et_info.getText().toString().trim();

        // Check if the fields are filled in
        // TODO: Make toast notifications individually
        /*
        if(location.length() == 0 || date.length() == 0 || start_time.length() == 0 || end_time.length() == 0){
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_LONG).show();
            return;
        }*/

        if(location.equals("Where are you eating?") || date.equals("Date?") ||
                start_time.equals("Start Time?") || end_time.equals("End Time?")){
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_LONG).show();
            return;
        }

        String startDateString = date + " " + start_time;
        String endDateString = date + " " + end_time;

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm"); //Custom time format
        formatter.setTimeZone(TimeZone.getDefault()); // Set timezone to device's

        Date startDate = null;
        Date endDate = null;
        try {
            startDate = formatter.parse(startDateString);
            endDate = formatter.parse(endDateString);
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
        if(startDate.before(new Date())){
            Toast.makeText(this, "Event cannot be in the past.", Toast.LENGTH_LONG).show();
            return;
        }

        // Set the formatter's time zone to UTC to convert the date object to this timezone
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        String f_start_time = formatter.format(startDate);
        String f_end_time = formatter.format(endDate);

        //addEvent(location, date, f_start_time, f_end_time, info);

        // Temporary workaround for now
        addEvent(location, date, startDateString, endDateString, info);
    }

    /**
     *
     */
    private void addEvent(String location, String date, String start_time, String end_time, String info){
        if(mAddTask != null){
            Toast.makeText(this, "Adding already in progress. Please wait.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        mAddTask = new ServerAddTask(location, start_time, end_time, info);
        mAddTask.execute((Void) null);

    }

    /**
     * Set the Date Picker EditText to the day's date
     */
    private void setTodaysDate(){
        //TODO
    }

    /**
     * Gets JSON String response from server and handles an error if there is one.
     * if not...
     *
     * @param response (JSON String from the server)
     */
    public boolean processResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            // If there was some issue with the log in process
            if(jsonObject.has("error")){
                Log.w("URL Process", "There is an error tag!");
                String errorMessage = "Not able to add event. " + jsonObject.getString("error");
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                return false;
            }

            // Event successfully created
            else{
                Toast.makeText(getApplicationContext(), "Event Successfully Created!", Toast.LENGTH_SHORT).show();
                return true;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class ServerAddTask extends AsyncTask<Void, Void, String> {

        private final String mLocation;
        private final String mStartTime;
        private final String mEndTime;
        private final String mInfo;
        private final String mUserEmail;
        private final String mUserName;

        ServerAddTask(String location, String start_time, String end_time, String info) {
            mLocation = location;
            mStartTime = start_time;
            mEndTime = end_time;
            mInfo = info;


            SharedPreferences sharedPref = getApplicationContext().
                    getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
            mUserEmail = sharedPref.getString(getString(R.string.preferences_user_email), "");
            mUserName = sharedPref.getString(getString(R.string.preferences_user_name), "");
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .authority(getString(R.string.host_name))
                        .path("creation/event")
                        .appendQueryParameter("user_email", mUserEmail)
                        .appendQueryParameter("user_name", mUserName)
                        .appendQueryParameter("event_location", mLocation)
                        .appendQueryParameter("event_start_time", mStartTime)
                        .appendQueryParameter("event_end_time", mEndTime)
                        .appendQueryParameter("event_info", mInfo)
                        .build();

                URL url = new URL(uri.toString());
                Log.w("URI", uri.toString());


                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(false);
                urlConnection.setConnectTimeout(20000);
                urlConnection.setReadTimeout(20000);

                if (urlConnection.getResponseCode() == 200 || urlConnection.getResponseCode() == 201) {
                    BufferedReader br = new BufferedReader(new InputStreamReader
                            (urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    Log.w("URL", "Response:" + sb.toString());
                    return sb.toString();
                }
            } catch (MalformedURLException | ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // If response code is not 2xx or if something else went wrong.
            String jsonErrorResponse = "{\"error\":\"Something wen't wrong. Try again.\"}";
            return jsonErrorResponse;
        }

        @Override
        protected void onPostExecute(final String response) {
            mAddTask = null;
            boolean isSuccess = processResponse(response);
            progressDialog.dismiss();
            if(isSuccess) {
                Intent data = new Intent();
                data.putExtra("STATUS", "SUCCESS");
                setResult(RESULT_OK, data);
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mAddTask = null;
            progressDialog.dismiss();
        }
    }
}
