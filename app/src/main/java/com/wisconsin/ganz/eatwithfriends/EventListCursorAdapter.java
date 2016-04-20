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
        TextView eventTitle = (TextView) view.findViewById(R.id.event_title);
        TextView eventTimes = (TextView) view.findViewById(R.id.event_timings);

        eventTitle.setText(cursor.getString(1));

        eventTimes.setText(cursor.getString(2));
    }

}
