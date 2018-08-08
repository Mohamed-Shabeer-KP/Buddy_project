package com.boss.buddy.CloudDataFetch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.boss.buddy.BuildConfig;
import com.boss.buddy.MainMenu;
import com.boss.buddy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class RemoteConfigActivity extends AppCompatActivity {

    private int FLAG_SCHEDULE_AVAILABLE;
    private int FLAG_SCHEDULE_FINISHED;
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
        },5000);
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
                            Toast.makeText(RemoteConfigActivity.this, "Fetch Succeeded", Toast.LENGTH_SHORT).show();
                            mFirebaseRemoteConfig.activateFetched();
                            FLAG_SCHEDULE_AVAILABLE = Integer.parseInt(mFirebaseRemoteConfig.getString("SCHEDULE_AVAILABLE"));
                            FLAG_SCHEDULE_FINISHED = Integer.parseInt(mFirebaseRemoteConfig.getString("SCHEDULE_FINISHED"));

                        } else {
                            Toast.makeText(RemoteConfigActivity.this, "Fetch Failed", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
    }
    }



