package com.example.myapplication1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homerl;
    private ProgressBar loadingpb;
    private TextView citynameTv,temperatureTv,conditionTv;
    private TextInputEditText cityEdt;
    private ImageView backIV,iconIV,searchIV;
    private RecyclerView weatherRV;
    private ArrayList<wetherRVModel> wetherRVModelArrayList ;
    private weatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE=1;
    private String CityName;
    private static final String TAG = "MyActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);//full screen
        setContentView(R.layout.activity_main);
        homerl=findViewById(R.id.rlhome);
        loadingpb=findViewById(R.id.pbloading);
        citynameTv=findViewById(R.id.cityame);
        temperatureTv=findViewById(R.id.temprature);
        conditionTv=findViewById(R.id.tempnature);
        cityEdt=findViewById(R.id.editcity);
        backIV=findViewById(R.id.idback);
        iconIV=findViewById(R.id.tempnature_icon);
        searchIV=findViewById(R.id.search);
        weatherRV=findViewById(R.id.weather_recycle);
        wetherRVModelArrayList=new ArrayList<>();
        weatherRVAdapter=new weatherRVAdapter(this,wetherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);
        //Toast.makeText(MainActivity.this, "one", Toast.LENGTH_SHORT).show();

        //LocationManager is the main class through which your application can access location services on
        // Android. Similar to other system services, a reference can be obtained from calling the getSystemService()
        // method. If your application intends to receive location updates in the foreground (within an Activity),
        // you should usually perform this step in the onCreate() method.
        locationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
       // Toast.makeText(MainActivity.this, "locatio se baad", Toast.LENGTH_SHORT).show();

        CityName=getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(CityName);
        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city=cityEdt.getText().toString();
                if(city.isEmpty())
                {
                    Toast.makeText(MainActivity.this,"please enter city name",Toast.LENGTH_SHORT).show();
                }else
                    citynameTv.setText(city);
                    getWeatherInfo(city);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       if(requestCode==PERMISSION_CODE){
           if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
               Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
           else {
               Toast.makeText(this, "Please provide the Permissions", Toast.LENGTH_SHORT).show();
           finish();
           }

       }
    }

    //getBasecontext() - possible be destroyed when the activity is destroyed.
    //Locale.getDefault() -The default Locale is constructed statically(the one setup at jvm) at runtime for your application
    // process from the system property settings,so it will represent the Locale selected on that
    // device when the application was launched.
    // Locale is: An object that represents a specific geographical, political, or cultural region
    private String getCityName(double longitude, double latitude)
    {
        String cityNme="Not found";
        //Gocoder for geocoding- process of transforming a (latitude, longitude) coordinate into a (partial) address.
        Geocoder gcd=new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses=gcd.getFromLocation(latitude,longitude,10);
            for (Address adr : addresses)
            {
                if (adr!=null)
                {
                    String city=adr.getLocality();
                    if(city!=null && !city.equals(""))
                    {
                        cityNme=city;
                    }else {
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this,"Use city not found....", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }return cityNme;
    }
    private void getWeatherInfo(String cityNme)
    {
        cityNme="Not Found";
        String url="http://api.weatherapi.com/v1/forecast.json?key=585733c717d0409292e115248212412&q="+cityNme+"&days=1&aqi=yes&alerts=yes";
        citynameTv.setText(cityNme);
        RequestQueue requestQueue= Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null,new  Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingpb.setVisibility(View.GONE);
                homerl.setVisibility(View.VISIBLE);
                wetherRVModelArrayList.clear();
                try {
                    String temprature=response.getJSONObject("current").getString("temp_c");
                    temperatureTv.setText(temprature+"°c");
                    int isday=response.getJSONObject("current").getInt("is_day");
                    String condition=response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon=response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTv.setText(condition);
                    if(isday==1)
                    {
                        Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.istockphoto.com%2Fphoto%2Fheaven-cloud-sky-sunny-bright-for-future-wealth-fortune-day-concept-gm1015820498-273339623&psig=AOvVaw1PIT45kD4T8IYeqKZwFt3_&ust=1640896394496000&source=images&cd=vfe&ved=2ahUKEwix1fbG7Yn1AhUsx6ACHQzSAtMQjRx6BAgAEAk").into(backIV);
                    }else{
                        Picasso.get().load("https://www.google.com/imgres?imgurl=https%3A%2F%2Fmedia.istockphoto.com%2Fphotos%2Fdeep-space-background-picture-id178149253%3Fk%3D20%26m%3D178149253%26s%3D612x612%26w%3D0%26h%3DTJOJWolz2MJt-QLH0jPvbl-Bz-4ySIvSTVdKoLP1lfg%3D&imgrefurl=https%3A%2F%2Fwww.istockphoto.com%2Fphotos%2Fnight-sky&tbnid=4vwo5K_cpjDC9M&vet=12ahUKEwiV5NTi7Yn1AhWe2XMBHcELBQEQMygBegUIARC6AQ..i&docid=rDG2CY38lDUC8M&w=612&h=408&q=night%20background%20clouds%20%20free&hl=en-GB&ved=2ahUKEwiV5NTi7Yn1AhWe2XMBHcELBQEQMygBegUIARC6AQ").into(backIV);
                    }
                    JSONObject forecastobj=response.getJSONObject("forecast");
                    JSONObject forcast0=forecastobj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray=forcast0.getJSONArray("hour");

                    for(int i=0;i<hourArray.length();i++)
                    {
                        JSONObject hourObj=hourArray.getJSONObject(i);
                        String time=hourObj.getString("time");
                        String tempor=hourObj.getString("temp_c");
                        String img=hourObj.getJSONObject("condition").getString("icon");
                        String wind=hourObj.getString("wind");
                        wetherRVModelArrayList.add(new wetherRVModel(time,tempor,img,wind));

                    }
                    weatherRVAdapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                 //   Toast.makeText(MainActivity.this, "Error Response code: " + error.networkResponse.statusCode, Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "Please enter valid city name", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
