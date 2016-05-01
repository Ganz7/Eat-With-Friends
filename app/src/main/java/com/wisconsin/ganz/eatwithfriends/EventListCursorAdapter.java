package com.wisconsin.ganz.eatwithfriends;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/***
 * Custom Cursor Adapter for Event List
 */
public class EventListCursorAdapter extends CursorAdapter {

    public EventListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
        Button goButton = (Button) view.findViewById(R.id.button_go);
        goButton.setOnClickListener(goButtonListener);
        return view;
    }

    View.OnClickListener goButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View parentRow = (View) v.getParent().getParent();
            ListView listView = (ListView) parentRow.getParent();
            final int position = listView.getPositionForView(parentRow);
            final long chosen_ID = listView.getItemIdAtPosition(position);
            Toast.makeText(v.getContext(), "Position: "+chosen_ID, Toast.LENGTH_SHORT).show();
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
        TextView eventInfo = (TextView) view.findViewById(R.id.tv_info);

        eventUserEmail.setText(cursor.getString(1));
        eventLocation.setText(cursor.getString(2));
        eventInfo.setText(cursor.getString(3));
    }

}
