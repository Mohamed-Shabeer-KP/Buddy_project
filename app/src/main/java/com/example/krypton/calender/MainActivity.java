package com.example.krypton.calender;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.krypton.calender.Calander.ActivityAPI;
import com.example.krypton.calender.Calander.EventActivity;
import com.example.krypton.calender.CloudMessaging.CloudMessageActivity;
import com.example.krypton.calender.DataProcess.RemoteConfigActivity;
import com.example.krypton.calender.DataProcess.SharedPreferencesJava;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button B1;
    private Button B2;
    private Button B3;
    private Button B4;
    private Button B5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        B1= findViewById(R.id.b1);
        B2= findViewById(R.id.b2);
        B3= findViewById(R.id.b3);
        B4= findViewById(R.id.b4);
        B5= findViewById(R.id.b5);

        B1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(),ActivityAPI.class));
            }
        });
        B2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getApplicationContext(),EventActivity.class));
            }
        });

        B3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),RemoteConfigActivity.class));
            }
        });
        B4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CloudMessageActivity.class));
            }
        });
       B5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SharedPreferencesJava.class));
            }
        });

        //----------------CLOUD MESSAGING----------//
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
          /*
           if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        */
        //----------------CLOUD MESSAGING----------//
    }
}

