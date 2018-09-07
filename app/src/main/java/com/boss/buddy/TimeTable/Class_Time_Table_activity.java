package com.boss.buddy.TimeTable;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.boss.buddy.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Class_Time_Table_activity extends AppCompatActivity {

    private ImageView img;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class__time__table_activity);

        img = findViewById(R.id.imageView);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Images");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                url = String.valueOf(dataSnapshot.child("timetable").getValue());
                Glide.with(getApplicationContext()).load(url).into(img);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Class_Time_Table_activity.this, "FAILED TO FETCH FILE, PLEASE RELOAD APP AGAIN.", Toast.LENGTH_SHORT).show();
            }

        });

    }
}
