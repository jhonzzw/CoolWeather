package com.zzw.coolweather.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zzw.coolweather.MainActivity;
import com.zzw.coolweather.R;
import com.zzw.coolweather.WeatherActivity;
import com.zzw.coolweather.db.City;
import com.zzw.coolweather.db.Country;
import com.zzw.coolweather.db.Province;
import com.zzw.coolweather.util.HttpUtil;
import com.zzw.coolweather.util.Utilty;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/12/26.
 */

public class ChooseAreaFragment extends Fragment {
    //标记 省市县
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTRY=2;
    /**
     * 当前选中的级别
     */
    private int currentLevel=3;
    /**
     * 选中的省份
     */
    private Province selcetedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 选中的县
     */
    private Country selectedCountry;
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<Country> countryList;


    //导航栏上的控件
    private TextView title;
    private Button backButton;


    //ListView 的控件及adapter 资源
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<> ();

    private ProgressDialog progressDialog;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate (R.layout.fragment_choose_area_layout,container,false);
        //初始化控件
        title=view.findViewById (R.id.text_show);
        backButton=view.findViewById (R.id.back_button);
        //给ListView 初始化及设置Adapter
        listView=view.findViewById (R.id.list_view);
        adapter=new ArrayAdapter<String> (getContext (),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter (adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated (savedInstanceState);
        listView.setOnItemClickListener (new AdapterView.OnItemClickListener () {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selcetedProvince = provinceList.get (position);
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get (position);
                    queryCountries();
                }else if(currentLevel==LEVEL_COUNTRY){
                    selectedCountry=countryList.get (position);
                    String weatherId=selectedCountry.getWeatherId ();
                    if (getActivity () instanceof MainActivity){
                        Intent intent=new Intent (getActivity (), WeatherActivity.class);
                        intent.putExtra ("weatherId",weatherId);
                        startActivity (intent);
                        getActivity ().finish ();
                    }else if (getActivity () instanceof WeatherActivity){
                        WeatherActivity weatherActivity = (WeatherActivity) getActivity ();
                        weatherActivity.drawerLayout.closeDrawers ();
                        weatherActivity.refreshLayout.setRefreshing (true);
                        weatherActivity.requestWeather (weatherId);
                    }


                }

            }

        });
        backButton.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTRY){
                    queryCities ();
                }else if (currentLevel==LEVEL_CITY){
                    qureyProvince ();
                }
            }
        });
        qureyProvince();
    }

    private void queryCountries() {
        title.setText (selectedCity.getCityName ());
        backButton.setVisibility (View.VISIBLE);
        countryList=DataSupport.where ("cityId=?",String.valueOf (selectedCity.getId ())).find (Country.class);
        if (countryList.size ()>0){
            dataList.clear ();
            for (Country country:countryList){
                dataList.add (country.getCountryName ());
            }
            adapter.notifyDataSetChanged ();
            listView.setSelection (0);
            currentLevel=LEVEL_COUNTRY;
        }else {
            String address="http://guolin.tech/api/china/"+selcetedProvince.getProvinceCode ()+"/"+selectedCity.getCityCode ();
            qureyFormServer (address,"country");

        }
    }

    private void queryCities() {
        title.setText (selcetedProvince.getProvinceName ());
        backButton.setVisibility (View.VISIBLE);
        cityList= DataSupport.where ("provinceId=?", String.valueOf (selcetedProvince.getId () )).find (City.class);
        if (cityList.size ()>0){
            dataList.clear ();
            for (City city:cityList){
                dataList.add (city.getCityName ());
            }
            adapter.notifyDataSetChanged ();
            currentLevel=LEVEL_CITY;
        }else {
            String address="http://guolin.tech/api/china/"+selcetedProvince.getProvinceCode ();

            qureyFormServer (address,"city");
        }
    }

    private void qureyProvince() {
        title.setText ("中国");
        backButton.setVisibility (View.GONE);
        provinceList = DataSupport.findAll (Province.class);
        if (provinceList.size ()>0){
            dataList.clear ();
            for (Province province:provinceList){
                dataList.add (province.getProvinceName ());
            }
            adapter.notifyDataSetChanged ();
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            qureyFormServer(address,"province");

        }
    }

    private void qureyFormServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest (address, new Callback () {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity ().runOnUiThread (new Runnable () {
                    @Override
                    public void run() {
                        closeProgressDialog ();
                        Toast.makeText (getActivity (),"下载失败",Toast.LENGTH_SHORT).show ();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String textResponse=response.body ().string ();
                boolean result=false;
                if ("province".equals (type)){
                    //把下载的数据保存在数据库表里
                    result = Utilty.handleProvinceResponse (textResponse);
                }
                if ("city".equals (type)){
                    result=Utilty.handleCityResponse (textResponse,selcetedProvince.getId ());
                }
                if ("country".equals (type)){
                    result=Utilty.handleCountryResponse (textResponse,selectedCity.getId ());
                }
                if (result){
                    getActivity ().runOnUiThread (new Runnable () {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals (type)){
                                qureyProvince ();
                            }else if ("city".equals (type)){
                                queryCities ();
                            }else if("country".equals (type)){
                                queryCountries ();
                            }

                        }
                    });
                }
            }
        });
    }

    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss ();
        }
    }

    private void showProgressDialog() {
        if (progressDialog==null){
            progressDialog=new ProgressDialog (getActivity ());
            progressDialog.setMessage ("正在下载。。。。");
            progressDialog.setCanceledOnTouchOutside (false);
        }
        progressDialog.show ();
    }
}
