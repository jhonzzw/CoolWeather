package com.zzw.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/12/27.
 */

public class Forecast {
    @SerializedName ("date")
    public String data;
    @SerializedName ("cond")
    public More more;
    @SerializedName ("tmp")
    public Temperature temperature;
    public class Temperature{
        public String max;
        public String min;

        @Override
        public String toString() {
            return "max="+max+"min="+min;
        }
    }

    public class More{
        @SerializedName ("txt_d")
        public String info;

        @Override
        public String toString() {
            return "info="+info;
        }
    }

    @Override
    public String toString() {
        return "data="+data+more.toString ()+temperature.toString ();
    }
}
