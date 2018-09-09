package com.boss.buddy;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.boss.buddy.CalendarEvent.EventActivity;
import com.boss.buddy.TimeTable.Class_Time_Table_activity;


public class MainMenu extends AppCompatActivity {

    boolean exit = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button schedule_Button = findViewById(R.id.b_shedule);
        Button timetable_Button = findViewById(R.id.b_timetable);
        Button gdrive_Button = findViewById(R.id.b_gdrive);

        Bundle B = getIntent().getExtras();
        assert B != null;

        int FLAG_AVAILABLE = B.getInt("FLAG_AVAILABLE");
        int FLAG_FINISHED = B.getInt("FLAG_FINISHED");

        if (FLAG_AVAILABLE == 0 && FLAG_FINISHED == 0)
        {
            Toast.makeText(this, " FAILED TO CONNECT TO INTERNET", Toast.LENGTH_SHORT).show();
        }

        if (FLAG_AVAILABLE == 0 && FLAG_FINISHED == 1) {
            schedule_Button.setVisibility(View.INVISIBLE);
            SharedPreferences SPW = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = SPW.edit();
            editor.putInt(getString(R.string.StoredValue), 0);
            editor.apply();
        }

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        int defaultValue = Integer.parseInt(getResources().getString(R.string.DefaultValue));
        int SCHEDULE_DONE = sharedPref.getInt(getString(R.string.StoredValue), defaultValue);

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
}

