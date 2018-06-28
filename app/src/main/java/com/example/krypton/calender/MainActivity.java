package com.example.krypton.calender;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.krypton.calender.Calander.ActivityAPI;
import com.example.krypton.calender.Calander.EventActivity;


public class MainActivity extends AppCompatActivity {
    boolean exit = false;

    private RelativeLayout MainLayout;
    private Button ScheduleButton;
    private Button B3;
    private Button B4;

    public int FLAG_1;
    public int FLAG_2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainLayout = findViewById(R.id.mainlayout);

        Bundle B = getIntent().getExtras();
        FLAG_1 = B.getInt("FLAG_AVAILABLE");
        FLAG_2 = B.getInt("FLAG_DONE");


        if (FLAG_1 == 0 && FLAG_2 == 1) {
            SharedPreferences SPW = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = SPW.edit();
            editor.putInt(getString(R.string.StoredValue), FLAG_1);
            editor.commit();
        }
        SharedPreferences SPR = this.getPreferences(Context.MODE_PRIVATE);
        int DefaultValue = Integer.parseInt(getResources().getString(R.string.DefaultValue));
        int FinalVal = SPR.getInt(getString(R.string.StoredValue), DefaultValue);


        if (FLAG_1 == 1 && FinalVal == 1) {
            {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(250, 100, 30, 0);
                ScheduleButton = new Button(this);
                ScheduleButton.setText("SCHEDULE EXAM");
                final SharedPreferences SPW = this.getPreferences(Context.MODE_PRIVATE);
                ScheduleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ScheduleButton.setEnabled(false);
                        SharedPreferences.Editor editor = SPW.edit();
                        editor.putInt(getString(R.string.StoredValue), FLAG_1);
                        editor.commit();
                        startActivity(new Intent(getApplicationContext(), ActivityAPI.class));
                        ScheduleButton.setEnabled(true);
                    }
                });
                MainLayout.addView(ScheduleButton, layoutParams);
            }

            //----------------CLOUD MESSAGING----------//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create channel to show notifications.
                String channelId = getString(R.string.default_notification_channel_id);
                String channelName = getString(R.string.default_notification_channel_name);
                NotificationManager notificationManager =
                        getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_LOW));
            }
            //----------------CLOUD MESSAGING----------//
        }
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

