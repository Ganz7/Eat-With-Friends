package com.wisconsin.ganz.eatwithfriends;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/***
 * Custom Cursor Adapter for Event List
 */
public class EventListCursorAdapter extends CursorAdapter {

    // For progress dialog
    private ProgressDialog progressDialog;

    private EventsFetchTask mEventsTask = null;

    public EventListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
        Button goButton = (Button) view.findViewById(R.id.button_go);
        goButton.setOnClickListener(goButtonListener);

        setUpProgressDialog(parent.getContext());
        return view;
    }

    View.OnClickListener goButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View parentRow = (View) v.getParent().getParent().getParent();
            ListView listView = (ListView) parentRow.getParent();
            final int position = listView.getPositionForView(parentRow);
            final long chosen_ID = listView.getItemIdAtPosition(position);
            Toast.makeText(v.getContext(), "Position: "+chosen_ID, Toast.LENGTH_SHORT).show();

            Button button = (Button) v;
            // User is going now. So request has been made to mark as not-going.
            if(button.getText().toString().equals(HomeActivity.getContextOfApp().getString(R.string.button_going))){
                updateUserEventStatus(listView, chosen_ID, false);
            }
            // User is not going. But has requested to go.
            else{
                updateUserEventStatus(listView, chosen_ID, true);
            }
        }
    };

    /*
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        return convertView;
    }*/

    @Override
    /***
     * Map the values on to the list item
     */
    public void bindView(View view, Context context, Cursor cursor) {
        TextView eventUserEmail = (TextView) view.findViewById(R.id.tv_userEmail);
        TextView eventLocation = (TextView) view.findViewById(R.id.tv_location);
        TextView eventTime = (TextView) view.findViewById(R.id.tv_time);
        TextView eventDate = (TextView) view.findViewById(R.id.tv_date);
        Button goButton = (Button) view.findViewById(R.id.button_go);

        eventUserEmail.setText(cursor.getString(1));

        String[] placeInfo = cursor.getString(2).split("\\|");

        eventLocation.setText(placeInfo[0]);

        String time = cursor.getString(3) + " to " + cursor.getString(4);
        eventTime.setText(time);

        eventDate.setText(cursor.getString(5));
        boolean isGoing = Boolean.parseBoolean(cursor.getString(6));
        if(isGoing){
            goButton.setText(HomeActivity.getContextOfApp().getString(R.string.button_going));
            goButton.setTextColor(ContextCompat.getColor(context, R.color.materialGreen1));
        }
        else{
            goButton.setText(HomeActivity.getContextOfApp().getString(R.string.button_go));
            goButton.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
    }


    boolean updateUserEventStatus(ListView view, long event_ID, boolean isGoing){

        progressDialog.show();
        // TODO: get email from pref
        mEventsTask = new EventsFetchTask(view, HomeActivity.getUserPrefEmail(), event_ID, isGoing);
        mEventsTask.execute((Void) null);

        return false;
    }


    public void replaceLoader(ListView view, String response, Context ctx){
        mEventsTask = null;
        progressDialog.dismiss();

            /* Incredibly Hackish way! But this is a class project that cannot be given anymore time
             * Should fix this entire workflow in the future */
        Toast.makeText(ctx, "Status Successfully updated.", Toast.LENGTH_SHORT).show();
        HomeActivity.getfManager().beginTransaction()
                .replace(R.id.home_fragment_container,
                        MainFeedFragment.newInstance(HomeActivity.getUserPrefEmail(), "15"))
                .commit();
    }

    /**
     * Represents an asynchronous event fetching task used to pull events from
     * the server.
     */
    public class EventsFetchTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final long mEventID;
        private final ListView mView;
        private final Context mCtx;
        private final boolean mIsGoing;

        EventsFetchTask(ListView view, String email, long event_ID, boolean isGoing) {
            mEmail = email;
            mEventID = event_ID;
            mView = view;
            mCtx = view.getContext();
            mIsGoing = isGoing;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                Uri uri = new Uri.Builder()
                        .scheme("https")
                        .authority(mCtx.getString(R.string.host_name))
                        .path("events/user_event_status")
                        .appendQueryParameter("user_email", mEmail)
                        //.appendQueryParameter("row_count", String.valueOf(1))
                        .appendQueryParameter("event_id", String.valueOf(mEventID))
                        .appendQueryParameter("status", String.valueOf(mIsGoing))
                        .build();

                URL url = new URL(uri.toString());
                Log.w("ECL URI", uri.toString());

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
                    Log.w("ECL URL", "Response:" + sb.toString());
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
                replaceLoader(mView, response, mCtx);
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
                    String errorMessage = "Not able to update status. " + jsonObject.getString("error");
                    Toast.makeText(mCtx.getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public void setUpProgressDialog(Context ctx){
        // Set up the Progress Dialog object
        progressDialog = new ProgressDialog(ctx);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Updating...");
    }

}

