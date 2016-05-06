package com.wisconsin.ganz.eatwithfriends;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.util.Date;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFeedFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USER_EMAIL = "aUserEmail";
    private static final String ARG_ROW_COUNT = "aRowCount";

    private String mUserEmail;
    private String mRowCount;

    private OnFragmentInteractionListener mListener;

    private EventsFetchTask mEventsTask = null;
    private DetailsFetchTask mDetailsTask = null;

    // View and Adapter Objects
    private ListView eventListView;
    private EventListCursorAdapter eventAdapter;

    // For progress dialog
    private ProgressDialog progressDialog;

    public MainFeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uemail User Email
     * @param rcount Row Count
     * @return A new instance of fragment MainFeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFeedFragment newInstance(String uemail, String rcount) {
        MainFeedFragment fragment = new MainFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_EMAIL, uemail);
        args.putString(ARG_ROW_COUNT, rcount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserEmail = getArguments().getString(ARG_USER_EMAIL);
            mRowCount = getArguments().getString(ARG_ROW_COUNT);
            Log.i("Frag", "Inside Fragment Create!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_feed, container, false);
        eventListView = (ListView) view.findViewById(R.id.event_list);
        eventListView.addHeaderView(new View(getActivity()));
        eventListView.addFooterView(new View(getActivity()));

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getEventDetails(id);
            }
        });

        setUpProgressDialog();
        getEvents();

        return view;
    }

    public void getEventDetails(long id){
        progressDialog.show();

        mDetailsTask = new DetailsFetchTask(id);
        mDetailsTask.execute((Void) null);
    }

    public void setUpProgressDialog(){
        // Set up the Progress Dialog object
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Get events from the REST API or from the SQLite Cache
     */
    private void getEvents(){

        progressDialog.show();

        mEventsTask = new EventsFetchTask(mUserEmail, mRowCount);
        mEventsTask.execute((Void) null);
    }

    /**
     * Populate the Event List Adapter
     *
     */
    protected void parseStringAndPopulateList(String result){

        JSONObject eventResult = null;
        JSONArray eventJsonArray = null;
        JSONArray eventStatusJsonArray = null;
        String[] columns = new String[] {"_id", "user_email", "user_name", "event_location",
                "event_start_time", "event_end_time", "event_date", "event_status", "event_info"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);

        try {
            eventResult = new JSONObject(result);
            eventJsonArray =  eventResult.getJSONArray("result");
            eventStatusJsonArray = eventResult.getJSONArray("user_status_result");

            for(int i=0; i<eventJsonArray.length(); i++){
                JSONObject eventObject = eventJsonArray.getJSONObject(i);

                Integer eventID = (Integer) eventObject.get("_event_id");
                String userEmail = eventObject.getString("user_email");
                String userName = eventObject.getString("user_name");
                String eventLocation = eventObject.getString("event_location");
                String eventInfo = eventObject.getString("event_info");


                matrixCursor.addRow(new Object[] {eventID,
                                                userEmail,
                                                userName,
                                                eventLocation,
                                                getTime(eventObject.getString("event_start_time")),
                                                getTime(eventObject.getString("event_end_time")),
                                                getDate(eventObject.getString("event_start_time")),
                                                isAttending(eventStatusJsonArray, eventID),
                                                eventInfo
                                                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (matrixCursor != null && matrixCursor.moveToFirst()) {
            eventAdapter = new EventListCursorAdapter(getActivity(), matrixCursor, 0);
            eventListView.setAdapter(eventAdapter);
        }
    }

    private boolean isAttending(JSONArray statusArray, int eventID){
        try {
            for (int i = 0; i < statusArray.length(); i++) {
                JSONObject obj = statusArray.getJSONObject(i);
                if(obj.getInt("event_id") == eventID) {
                    return obj.getBoolean("user_attendance");
                }
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return false;
    }

    private Date getDateObject(String input){
        input = input.replace("T", " ").split("\\.")[0];

        SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
            date = f1.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    public String getTime(String input){
        Date date = getDateObject(input);
        SimpleDateFormat f2 = new SimpleDateFormat("HH:mm"); //Custom time format
        f2.setTimeZone(TimeZone.getDefault());
        return f2.format(date);
    }
    public String getDate(String input){
        Date date = getDateObject(input);
        SimpleDateFormat f2 = new SimpleDateFormat("yyyy/MM/dd"); //Custom time format
        f2.setTimeZone(TimeZone.getDefault());
        return f2.format(date);
    }

    /**
     * Represents an asynchronous event fetching task used to pull events from
     * the server.
     */
    public class EventsFetchTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mRowCount;

        EventsFetchTask(String email, String rowCount) {
            mEmail = email;
            mRowCount = rowCount;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .authority(getString(R.string.host_name))
                        .path("events")
                        .appendQueryParameter("user_email", mEmail)
                        .appendQueryParameter("row_count", mRowCount)
                        .build();

                URL url = new URL(uri.toString());
                Log.w("URI", uri.toString());

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
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

            // If response code is not 2xx or if something else wen't wrong.
            return "{\"error\":\"Something wen't wrong. Try again.\"}";
        }

        @Override
        protected void onPostExecute(final String response) {
            if(!hasError(response)){
                parseStringAndPopulateList(response);
            }
            mEventsTask = null;
            progressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            mEventsTask = null;
            progressDialog.dismiss();
        }

        /**
         * Checks if the server JSON response has any error
         * @param response
         * @return
         */
        protected boolean hasError(String response){
            try {
                JSONObject jsonObject = new JSONObject(response);
                if(jsonObject.has("error")) {
                    String errorMessage = "Not able to fetch events. " + jsonObject.getString("error");
                    Toast.makeText(getActivity().getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void parseDetailsAndDisplay(String response){
        mDetailsTask = null;
        progressDialog.dismiss();

        JSONObject result = null;
        JSONObject details = null;
        JSONArray list = null;
        Intent detailsIntent = new Intent(getActivity(), EventDetailsActivity.class);

        try {
            result = new JSONObject(response);
            details = result.getJSONObject("event_details");
            list = result.getJSONArray("user_list");

            detailsIntent.putExtra("event_location", details.getString("event_location"));
            detailsIntent.putExtra("event_info", details.getString("event_info"));
            detailsIntent.putExtra("event_start_time", getTime(details.getString("event_start_time")));
            detailsIntent.putExtra("event_end_time", getTime(details.getString("event_end_time")));
            detailsIntent.putExtra("event_start_date", getDate(details.getString("event_start_time")));

            detailsIntent.putExtra("response", response);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        startActivity(detailsIntent);
    }

    public class DetailsFetchTask extends AsyncTask<Void, Void, String> {

        private final long mEventID;

        DetailsFetchTask(long id) {
            mEventID = id;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .authority(getString(R.string.host_name))
                        .path("events/details")
                        .appendQueryParameter("event_id", String.valueOf(mEventID))
                        .build();

                URL url = new URL(uri.toString());
                Log.w("URI", uri.toString());

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
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

            // If response code is not 2xx or if something else wen't wrong.
            return "{\"error\":\"Something wen't wrong. Try again.\"}";
        }

        @Override
        protected void onPostExecute(final String response) {
            if(!hasError(response)){
                parseDetailsAndDisplay(response);
            }
            else {
                mDetailsTask = null;
                progressDialog.dismiss();
            }
        }

        @Override
        protected void onCancelled() {
            mDetailsTask = null;
            progressDialog.dismiss();
        }

        protected boolean hasError(String response){
            try {
                JSONObject jsonObject = new JSONObject(response);
                if(jsonObject.has("error")) {
                    String errorMessage = "Not able to fetch events. " + jsonObject.getString("error");
                    Toast.makeText(getActivity().getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
