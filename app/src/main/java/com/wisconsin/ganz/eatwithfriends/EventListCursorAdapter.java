package com.wisconsin.ganz.eatwithfriends;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/***
 * Custom Cursor Adapter for Event List
 */
public class EventListCursorAdapter extends CursorAdapter {

    public EventListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
    }

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
