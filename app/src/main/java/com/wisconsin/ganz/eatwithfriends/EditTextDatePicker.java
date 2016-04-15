package com.wisconsin.ganz.eatwithfriends;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

/**
 * Class to handle listen events on a EditText and call DatePicker
 */
public class EditTextDatePicker implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    EditText editText;
    private int day;
    private int month;
    private int year;
    private Context context;

    public EditTextDatePicker(Context context, int editTextViewID) {
        Activity activity = (Activity) context;
        this.editText = (EditText) activity.findViewById(editTextViewID);
        this.editText.setOnClickListener(this);
        this.context = context;
    }
}