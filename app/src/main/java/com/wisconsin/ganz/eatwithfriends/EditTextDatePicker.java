package com.wisconsin.ganz.eatwithfriends;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Class to handle listen events on a EditText and call DatePicker
 */
public class EditTextDatePicker implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    Button editText;
    private int day;
    private int month;
    private int year;
    private Context context;

    public EditTextDatePicker(Context context, int editTextViewID) {
        Activity activity = (Activity) context;
        this.editText = (Button) activity.findViewById(editTextViewID);
        this.editText.setOnClickListener(this);
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        Calendar myCalendar = Calendar.getInstance();
        new DatePickerDialog(context, this, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear;
        this.day = dayOfMonth;

        updateEditText();
    }

    private void updateEditText(){
        StringBuilder date = new StringBuilder();
        date.append(month+1).append("/").append(day).append("/").append(year).append(" ");
        editText.setText(date);
    }
}