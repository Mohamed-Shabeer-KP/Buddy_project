package com.example.krypton.calender.DataProcess;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.krypton.calender.R;

public class SharedPreferencesJava extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_preferences_java);

        String newval="100";

      /*  SharedPreferences SPW = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = SPW.edit();
        editor.putString(getString(R.string.storedval), newval);
        editor.commit();


        SharedPreferences SPR = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.def);
        String FinalVal = SPR.getString(getString(R.string.storedval), defaultValue);

        TextView T1;
        T1=findViewById(R.id.t1);
        T1.setText(FinalVal);
    */}
}
