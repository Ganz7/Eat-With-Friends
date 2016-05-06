package com.wisconsin.ganz.eatwithfriends;

import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EventDetailsActivity extends AppCompatActivity {

    // View and Adapter Objects
    private ListView friendListView;
    private FriendListCursorAdapter friendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        friendListView = (ListView) findViewById(R.id.friend_list);
        setTitle("Event Details");

        setViews(getIntent());
    }

    public void setViews(Intent detailsIntent){
        TextView location = (TextView) findViewById(R.id.tv_location);
        TextView address = (TextView) findViewById(R.id.tv_address);
        TextView info = (TextView) findViewById(R.id.tv_info_content);
        TextView time = (TextView) findViewById(R.id.tv_time);
        TextView date = (TextView) findViewById(R.id.tv_date);

        String[] placeInfo = detailsIntent.getStringExtra("event_location").split("\\|");

        location.setText(placeInfo[0]);
        address.setText(placeInfo[1]);
        info.setText(detailsIntent.getStringExtra("event_info"));

        String timeString = detailsIntent.getStringExtra("event_start_time") + " to "
                + detailsIntent.getStringExtra("event_end_time");
        time.setText(timeString);

        date.setText(detailsIntent.getStringExtra("event_start_date"));

        setList(detailsIntent.getStringExtra("response"));
    }

    public void setList(String response){
        Log.w("DETAILS", response);
        JSONObject result = null;
        JSONArray jsonArray = null;
        String[] columns = new String[] {"_id","user_name", "user_email"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);

        try {
            result = new JSONObject(response);
            jsonArray = result.getJSONArray("user_list");

            for(int i=0; i<jsonArray.length(); i++) {
                JSONObject friend = jsonArray.getJSONObject(i);

                String userName = friend.getString("user_name");
                String userEmail = friend.getString("user_email");

                matrixCursor.addRow(new Object[]{i,
                        userName,
                        userEmail});
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (matrixCursor != null && matrixCursor.moveToFirst()) {
            friendsAdapter = new FriendListCursorAdapter(this, matrixCursor, 0);
            friendListView.setAdapter(friendsAdapter);
        }

    }
}
