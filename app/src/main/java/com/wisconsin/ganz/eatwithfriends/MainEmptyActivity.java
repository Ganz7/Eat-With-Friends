package com.wisconsin.ganz.eatwithfriends;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MainEmptyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /* Logic here to check if user is already logged in */

        SharedPreferences sharedPref = getApplicationContext().
                getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean(getString(R.string.preferences_user_logged_in), false);

        Log.w("Login", "Log in Status: "+isLoggedIn);
        // If user is not logged in
        // Log them in or register them
        if(!isLoggedIn) {
            Intent loginIntent = new Intent(MainEmptyActivity.this, LoginActivity.class);

            /* Call finish on MainEmptyActivity so that it does not become active on
             * back button press from LoginActivity
             * Other alternative is to use android:noHistory="true"
             */
            MainEmptyActivity.this.finish();

            startActivity(loginIntent);
        }

        // Else skip the login/register activity
        else{
            Intent homeIntent = new Intent(MainEmptyActivity.this, HomeActivity.class);

            /* Call finish on MainEmptyActivity so that it does not become active on
             * back button press from HomeActivity
             */
            MainEmptyActivity.this.finish();

            startActivity(homeIntent);
        }
    }
}
