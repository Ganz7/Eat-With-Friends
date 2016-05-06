package com.wisconsin.ganz.eatwithfriends;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainFeedFragment.OnFragmentInteractionListener,
            FriendListFragment.OnFragmentInteractionListener{

    private static String prefUserEmail;
    private static String prefUserName;

    private static Context contextOfApp;
    private static FragmentManager fManager;

    // Request Codes
    private static int CREATE_ACTIVITY_RESULT  = 10;

    // Constants
    private final static int NUMBER_OF_EVENTS = 25;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        contextOfApp = getApplicationContext();
        fManager = getSupportFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addEvent = new Intent(HomeActivity.this, CreateEvent.class);
                startActivityForResult(addEvent, CREATE_ACTIVITY_RESULT);
            }
        });

        setPreferenceInformation();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_feed);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        TextView mainUserEmail = (TextView) header.findViewById(R.id.tv_main_user_email);
        TextView mainUserName = (TextView) header.findViewById(R.id.tv_main_user_name);
        mainUserEmail.setText(prefUserEmail);
        mainUserName.setText(prefUserName);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.home_fragment_container,
                        MainFeedFragment.newInstance(prefUserEmail, String.valueOf(NUMBER_OF_EVENTS)))
                .commit();
    }

    public static Context getContextOfApp(){
        return contextOfApp;
    }
    public static FragmentManager getfManager(){
        return fManager;
    }
    public static String getPrefUserName() {return prefUserName;}
    public static String getUserPrefEmail(){
        return prefUserEmail;
    }

    /**
     * Set the private preference variables from SharedPreferences
     */
    public void setPreferenceInformation(){
        SharedPreferences sharedPref = getApplicationContext().
                getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        prefUserEmail = sharedPref.getString(getString(R.string.preferences_user_email), "");
        prefUserName = sharedPref.getString(getString(R.string.preferences_user_name), "");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_feed) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.home_fragment_container,
                            MainFeedFragment.newInstance(prefUserEmail, String.valueOf(NUMBER_OF_EVENTS)))
                    .commit();
        } else if (id == R.id.nav_my_events) {

        } else if (id == R.id.nav_friends) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.home_fragment_container,
                            FriendListFragment.newInstance())
                    .commit();

        } else if (id == R.id.nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logout(){
        SharedPreferences sharedPref = getApplicationContext().
                getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Change User Details in preferences
        editor.putString(getString(R.string.preferences_user_email), "");
        editor.putString(getString(R.string.preferences_user_name), "");
        editor.putBoolean(getString(R.string.preferences_user_logged_in), false);

        editor.commit();

        Toast.makeText(getApplicationContext(), "You are logged out", Toast.LENGTH_SHORT).show();

        Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);

        // Clear the entire stack
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_ACTIVITY_RESULT) {
            if (resultCode == RESULT_OK) {

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.home_fragment_container,
                                MainFeedFragment.newInstance(prefUserEmail, String.valueOf(NUMBER_OF_EVENTS)))
                        .commitAllowingStateLoss();
                        //.commit();
                Log.w("ACT Result", "Got a result.");
            }
            else{
                Log.w("ACT Result", "Got a result. Code is not OK.");
            }
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        //Empty for now
    }
}
