package com.zzw.coolweather.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.zzw.coolweather.gson.Weather;
import com.zzw.coolweather.util.HttpUtil;
import com.zzw.coolweather.util.Utilty;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException ("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //实现每8小时从服务器请求天气数据和图片数据保存到本地
        requestWeather();
        requestBingPic();
        AlarmManager alarmManager=(AlarmManager) getSystemService (ALARM_SERVICE);
        int anHour=8*60*60*1000;
        long triggerAtTime= SystemClock.elapsedRealtime ()+anHour;
        Intent intent1=new Intent (this,AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService (this, 0, intent1, 0);
        alarmManager.set (AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand (intent, flags, startId);
    }

    private void requestBingPic() {
        String bingPicUrl="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest (bingPicUrl, new Callback () {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText (AutoUpdateService.this,"请求数据失败",Toast.LENGTH_SHORT).show ();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic=response.body ().string ();
                SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences (AutoUpdateService.this);
                SharedPreferences.Editor editor = sharedPreferences.edit ();
                editor.putString ("bing_pic",bingPic);
                editor.apply ();
            }
        });



    }

    private void requestWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences (this);
        String weatherString = preferences.getString ("weather", null);
        if (weatherString!=null){
            final Weather weather = Utilty.handleWeatherResponse (weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=fc6f9a963dc8401a9c2bb519f8335dc3";
            HttpUtil.sendOkHttpRequest (weatherUrl, new Callback () {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText (AutoUpdateService.this,"请求数据失败",Toast.LENGTH_SHORT).show ();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String weatherString=response.body ().string ();
                    Weather weather1=Utilty.handleWeatherResponse (weatherString);
                    if (weatherString!=null&&"ok".equals (weather.status)){
                        SharedPreferences preferences1 = PreferenceManager.getDefaultSharedPreferences (AutoUpdateService.this);
                        SharedPreferences.Editor editor = preferences1.edit ();
                        editor.putString ("weather",weatherString);
                        editor.apply ();
                    }
                }
            });
        }
    }
}
