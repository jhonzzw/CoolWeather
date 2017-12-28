package com.zzw.coolweather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.zzw.coolweather.gson.Forecast;
import com.zzw.coolweather.gson.Weather;
import com.zzw.coolweather.util.HttpUtil;
import com.zzw.coolweather.util.Utilty;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/12/27.
 */

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherlayout;
    private TextView titleCityName,titleUpadeTime;
    private TextView nowdegress,nowWeatherInfo;
    private LinearLayout forecastLayout;
    private TextView aqiText,pm25Text;
    private TextView comfortText,carWashText,sportText;
    private SharedPreferences preferences;
    //背景图
    private ImageView bingPicImage;
    @Override
    public void onCreate(Bundle savedInstanceStatee) {
        super.onCreate (savedInstanceStatee);
        setContentView (R.layout.activity_weather);
      /*  if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow ().getDecorView ();
            decorView.setSystemUiVisibility (View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow ().setStatusBarColor (Color.TRANSPARENT);
        }*/

        //控件初始化
        weatherlayout=findViewById (R.id.weather_layout);
        titleCityName=findViewById (R.id.title_city);
        titleUpadeTime=findViewById (R.id.title_time);
        nowdegress=findViewById (R.id.degree_text);
        nowWeatherInfo=findViewById (R.id.weather_info_text);
        forecastLayout=findViewById (R.id.forecast_layout);

        aqiText=findViewById (R.id.aqi_text);
        pm25Text=findViewById (R.id.pm25_text);

        comfortText=findViewById (R.id.comfort_text);
        carWashText=findViewById (R.id.car_wash_text);
        sportText=findViewById (R.id.sport_text);

        bingPicImage=findViewById (R.id.bing_pic_image);
        preferences= PreferenceManager.getDefaultSharedPreferences (this);
        //加载背景图片
        String bing_pic = preferences.getString ("bing_pic", null);
        if (bing_pic!=null){
            Glide.with (this).load (bing_pic).into (bingPicImage);
        }else {
            loadBingPic();
        }
        //加载天气详情
        String weatherString = preferences.getString ("weather", null);
        if (weatherString!=null){
            Weather weather = Utilty.handleWeatherResponse (weatherString);
            showWeatherInfo (weather);
        }else {
            //无缓存去服务器查询
            String weatherId=getIntent ().getStringExtra ("weatherId");
            weatherlayout.setVisibility (View.INVISIBLE);
            requestWeather(weatherId);

        }
    }

    private void loadBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest (requestBingPic, new Callback () {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace ();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingpic=response.body ().string ();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences (WeatherActivity.this);
                SharedPreferences.Editor editor = preferences.edit ();
                editor.putString ("bing_pic",bingpic);
                editor.apply ();
                runOnUiThread (new Runnable () {
                    @Override
                    public void run() {
                        Glide.with (WeatherActivity.this).load (bingpic).into (bingPicImage);
                    }
                });
            }
        });
    }

    private void requestWeather(final String weatherId) {
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=fc6f9a963dc8401a9c2bb519f8335dc3";
        HttpUtil.sendOkHttpRequest (weatherUrl, new Callback () {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread (new Runnable () {
                    @Override
                    public void run() {
                        Toast.makeText (WeatherActivity.this,"网络请求失败",Toast.LENGTH_SHORT).show ();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String weatherString=response.body ().string ();
                final Weather weather = Utilty.handleWeatherResponse (weatherString);
                runOnUiThread (new Runnable () {
                    @Override
                    public void run() {
                        if (weather!=null&&"ok".equals (weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences (WeatherActivity.this).edit ();
                            editor.putString ("weather",weatherString);
                            editor.apply ();
                            showWeatherInfo(weather);
                        }
                    }
                });
            }
        });
        loadBingPic ();
    }

    private void showWeatherInfo(Weather weather) {
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split (" ")[1];
        titleCityName.setText (cityName);
        titleUpadeTime.setText (updateTime);

        String degress=weather.now.tmperature+"°C";
        String weatherInfo=weather.now.more.info;
        nowWeatherInfo.setText (weatherInfo);
        nowdegress.setText (degress);
        forecastLayout.removeAllViews ();
        for (Forecast forecast:weather.forecastList) {
            View view = LayoutInflater.from (WeatherActivity.this).inflate (R.layout.forecast_item, forecastLayout, false);
            TextView dataText = view.findViewById (R.id.date_text);
            TextView infoText = view.findViewById (R.id.info_text);
            TextView maxText = view.findViewById (R.id.max_text);
            TextView minText = view.findViewById (R.id.min_text);
            dataText.setText (forecast.data);
            infoText.setText (forecast.more.info);
            maxText.setText (forecast.temperature.max);
            minText.setText (forecast.temperature.min);
            forecastLayout.addView (view);
        }

        if (weather.aqi!=null){
            String aqi=weather.aqi.city.aqi;
            String pm25=weather.aqi.city.pm25;
            aqiText.setText (aqi);
            pm25Text.setText (pm25);
            }

        comfortText.setText ("舒适度："+weather.suggestion.comFort.info);
        carWashText.setText ("洗车指数："+weather.suggestion.carWash.info);
        sportText.setText ("运动指数："+weather.suggestion.sport.info);
        weatherlayout.setVisibility (View.VISIBLE);

    }
}
