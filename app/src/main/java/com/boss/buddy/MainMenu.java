package com.boss.buddy;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.boss.buddy.CalendarEvent.EventActivity;


public class MainMenu extends AppCompatActivity {
    boolean exit = false;
    private int FLAG_AVAILABLE;
    private int FLAG_FINISHED;



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle B = getIntent().getExtras();
        assert B != null;
        FLAG_AVAILABLE = B.getInt("FLAG_AVAILABLE");
        FLAG_FINISHED = B.getInt("FLAG_FINISHED");

        Button scheduleButton = findViewById(R.id.bshedule);

        if (FLAG_AVAILABLE == 0 && FLAG_FINISHED == 0)
        {
            setContentView(R.layout.activity_main);
            Toast.makeText(this, "INTERNET CONNECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }


        if (FLAG_AVAILABLE == 0 && FLAG_FINISHED == 1) {
            SharedPreferences SPW = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = SPW.edit();
            editor.putInt(getString(R.string.StoredValue), FLAG_AVAILABLE);
            editor.apply();
        }

            SharedPreferences SPR = this.getPreferences(Context.MODE_PRIVATE);
            int DefaultValue = Integer.parseInt(getResources().getString(R.string.DefaultValue));
            int SCHEDULE_DONE = SPR.getInt(getString(R.string.StoredValue), DefaultValue);



        if (FLAG_AVAILABLE == 1 && SCHEDULE_DONE == 0 )
            {
                scheduleButton.setText("SET SCHEDULE");
                scheduleButton.setVisibility(View.VISIBLE);



                final SharedPreferences SPW = this.getPreferences(Context.MODE_PRIVATE);
                scheduleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        SharedPreferences.Editor editor = SPW.edit();
                        editor.putInt(getString(R.string.StoredValue), FLAG_AVAILABLE);
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
                scheduleButton.setText("SHOW SCHEDULE");
                scheduleButton.setVisibility(View.VISIBLE);

                scheduleButton.setOnClickListener(new View.OnClickListener() {
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
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Onooode Alojichitu Pore",
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

