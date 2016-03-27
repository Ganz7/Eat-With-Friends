package com.wisconsin.ganz.eatwithfriends;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainEmptyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /* Logic here to check if user is already logged in */

        // If user is not logged in
        // Log them in or register them
        Intent loginIntent = new Intent(MainEmptyActivity.this, LoginActivity.class);

        // Call finish on MainEmptyActivity so that it does not become active on
        // back button press from LoginActivity
        // Other alternative is to use android:noHistory="true"
        MainEmptyActivity.this.finish();

        startActivity(loginIntent);

        // Else
        // skip the login/register activity
    }
}
