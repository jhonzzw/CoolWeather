package com.zzw.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.zzw.coolweather.db.City;
import com.zzw.coolweather.db.Country;
import com.zzw.coolweather.db.Province;
import com.zzw.coolweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/12/26.
 */

public class Utilty {
    /**
     * 解析和处理服务器返回的省级数据
     * @param reponse
     * @return
     */
    public static boolean handleProvinceResponse(String reponse){
        if (!TextUtils.isEmpty (reponse)) {
            try{
                JSONArray jsonArray = new JSONArray (reponse);
                for (int i=0;i<jsonArray.length ();i++){
                    JSONObject object= (JSONObject) jsonArray.get (i);
                    Province province=new Province ();
                    province.setProvinceName (object.getString ("name"));
                    province.setProvinceCode (object.getInt ("id"));
                    province.save ();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace ();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     * @param response
     * @param provinceId
     * @return
     */

    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty (response)){
            try{
                JSONArray jsonArray = new JSONArray (response);
                for (int i=0;i<jsonArray.length ();i++){
                    JSONObject object = (JSONObject)jsonArray.get (i);
                    City city = new City ();
                    city.setCityName (object.getString ("name"));
                    city.setCityCode (object.getInt ("id"));
                    city.setProvinceId (provinceId);
                    city.save ();
                }
                return true;
            }catch (JSONException e){

            }
        }
        return false ;
    }

    /**
     * 根据服务器返回数据，解析县级数据
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountryResponse(String response,int cityId){
        if (!TextUtils.isEmpty (response)){
            try {
                JSONArray jsonArray = new JSONArray (response);
                for (int i=0;i<jsonArray.length ();i++){
                    JSONObject o = (JSONObject) jsonArray.get (i);
                    Country country = new Country ();
                    country.setCountryName (o.getString ("name"));
                    country.setWeatherId (o.getString ("weather_id"));
                    country.setCityId (cityId);
                    country.save ();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace ();
            }
        }
        return false;
    }
    public static Weather handleWeatherResponse(String response){
        try {
            JSONArray heWeather = new JSONObject (response).getJSONArray ("HeWeather");
            String weatherContent = heWeather.getJSONObject (0).toString ();
            Log.i ("tag",weatherContent);
            return new Gson ().fromJson (weatherContent,Weather.class);
        }catch (JSONException e){
            e.printStackTrace ();
        }

        return null;
    }
}
