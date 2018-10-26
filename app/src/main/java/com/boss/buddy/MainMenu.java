package com.boss.buddy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.boss.buddy.CalendarEvent.EventActivity;
import com.boss.buddy.TimeTable.Class_Time_Table_activity;
import com.bumptech.glide.Glide;
import com.google.api.client.util.DateTime;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;


public class MainMenu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

    boolean exit = false;
    String message = "";

    @SuppressLint({"SetTextI18n", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("notification");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                message = (String) dataSnapshot.child("message").getValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }

        });

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final FloatingActionButton fab =  findViewById(R.id.fab);
           // fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        Button schedule_Button = findViewById(R.id.b_shedule);
        Button timetable_Button = findViewById(R.id.b_timetable);
        Button gdrive_Button = findViewById(R.id.b_gdrive);

        Bundle B = getIntent().getExtras();
        assert B != null;

        int FLAG_AVAILABLE = B.getInt("FLAG_AVAILABLE");
        int FLAG_FINISHED = B.getInt("FLAG_FINISHED");

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int defaultValue = Integer.parseInt(getResources().getString(R.string.DefaultValue));
        int SCHEDULE_DONE = sharedPref.getInt(getString(R.string.StoredValue), defaultValue);

        if (FLAG_AVAILABLE == 0 && FLAG_FINISHED == 0)
        {
            Toast.makeText(this, " FAILED TO CONNECT TO INTERNET", Toast.LENGTH_SHORT).show();
        }

        if (FLAG_AVAILABLE == 0 && FLAG_FINISHED == 1) {
            schedule_Button.setVisibility(View.INVISIBLE);

                if(SCHEDULE_DONE==1) {
                Intent Show = new Intent(getApplicationContext(), EventActivity.class);
                Bundle BShow = new Bundle();
                BShow.putInt("flag", 2);
                Show.putExtras(BShow);
                startActivity(Show);
            }

            SharedPreferences SPW = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = SPW.edit();
            editor.putInt(getString(R.string.StoredValue), 0);
            editor.apply();

        }

        if (FLAG_AVAILABLE == 1 && SCHEDULE_DONE == 0 )
            {
                schedule_Button.setText("SET SCHEDULE");
                schedule_Button.setVisibility(View.VISIBLE);

                schedule_Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt(getString(R.string.StoredValue), 1);
                        editor.apply();

                        Intent Show=new Intent(getApplicationContext(), EventActivity.class);
                        Bundle BShow=new Bundle();
                        BShow.putInt("flag",0);
                        Show.putExtras(BShow);
                        startActivity(Show);
                    }
                });
            }

            if (FLAG_AVAILABLE == 1 && SCHEDULE_DONE == 1 )
             {
                schedule_Button.setText("SHOW SCHEDULE");
                schedule_Button.setVisibility(View.VISIBLE);

                schedule_Button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent Show=new Intent(getApplicationContext(), EventActivity.class);
                        Bundle BShow=new Bundle();
                        BShow.putInt("flag",1);
                        Show.putExtras(BShow);
                        startActivity(Show);
                    }
                });
            }

        timetable_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tt_show=new Intent(getApplicationContext(), Class_Time_Table_activity.class);
                startActivity(tt_show);

            }
        });

        gdrive_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://gogreenmca.page.link/drive");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        //----------------CLOUD MESSAGING----------//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create channel to show notifications.
                String channelId = getString(R.string.default_notification_channel_id);
                String channelName = getString(R.string.default_notification_channel_name);
                NotificationManager notificationManager =
                        getSystemService(NotificationManager.class);
                assert notificationManager != null;
                notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_LOW));
            }
            //----------------CLOUD MESSAGING----------//
        }


    @Override
    public void onBackPressed() {


        if (exit) {
            System.exit(0);
        } else {
            Toast.makeText(this, "PRESS AGAIN TO EXIT",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {

            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent home=new Intent(getApplicationContext(), MainMenu.class);
            startActivity(home);
        }
        else if (id == R.id.nav_ttable) {
            Intent tt_show=new Intent(getApplicationContext(), Class_Time_Table_activity.class);
            startActivity(tt_show);
        }
        else if (id == R.id.nav_about) {
            Intent about=new Intent(getApplicationContext(), About.class);
            startActivity(about);
        }
        else if (id == R.id.nav_contact) {
            Intent contact=new Intent(getApplicationContext(), ContactUs.class);
            startActivity(contact);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

