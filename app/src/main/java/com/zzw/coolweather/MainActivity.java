package com.zzw.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences (this);
        String weatherString = preferences.getString ("weather", null);
        if (weatherString!=null){
            Intent intent=new Intent (this,WeatherActivity.class);
            startActivity (intent);
            finish ();
        }
    }
}
