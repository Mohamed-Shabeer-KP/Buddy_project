package com.boss.buddy.CloudDataFetch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.boss.buddy.BuildConfig;
import com.boss.buddy.MainMenu;
import com.boss.buddy.R;
import com.boss.buddy.TimeTable.Class_Time_Table_activity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class RemoteConfigActivity extends AppCompatActivity {

    private int FLAG_SCHEDULE_AVAILABLE;
    private int FLAG_SCHEDULE_FINISHED;
    private ImageView img;
    private String url;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_config);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        fetchValues();


        img = findViewById(R.id.splash_img);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Images");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                url = String.valueOf(dataSnapshot.child("splash").getValue());
                Glide.with(getApplicationContext()).load(url).into(img);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RemoteConfigActivity.this, "FAILED TO FETCH FILE, PLEASE RELOAD APP AGAIN.", Toast.LENGTH_SHORT).show();
            }

        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent StartMain=new Intent(getApplicationContext(),MainMenu.class);
                Bundle B=new Bundle();
                B.putInt("FLAG_AVAILABLE",FLAG_SCHEDULE_AVAILABLE);
                B.putInt("FLAG_FINISHED",FLAG_SCHEDULE_FINISHED);
                StartMain.putExtras(B);
                startActivity(StartMain);
                finish();
            }
        },4000);


    }

    private void fetchValues() {

        long cacheExpiration = 3600;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mFirebaseRemoteConfig.activateFetched();
                            FLAG_SCHEDULE_AVAILABLE = Integer.parseInt(mFirebaseRemoteConfig.getString("SCHEDULE_AVAILABLE"));
                            FLAG_SCHEDULE_FINISHED = Integer.parseInt(mFirebaseRemoteConfig.getString("SCHEDULE_FINISHED"));

                        }

                    }
                });
    }
    }



