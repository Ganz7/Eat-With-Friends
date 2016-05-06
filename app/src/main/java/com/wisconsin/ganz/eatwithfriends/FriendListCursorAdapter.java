package com.wisconsin.ganz.eatwithfriends;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class FriendListCursorAdapter extends CursorAdapter {

    public FriendListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_list_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView userName = (TextView) view.findViewById(R.id.tv_userName);
        TextView userEmail = (TextView) view.findViewById(R.id.tv_userEmail);

        userName.setText(cursor.getString(1));
        userEmail.setText(cursor.getString(2));
    }
}
