package com.mpho.weathertoday;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import static com.google.android.gms.location.LocationServices.*;
//import com.google.android.gms.instantapps.ActivityCompat;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private GoogleApiClient google_api_client;
    private Location user_location;
    private Double lattitude, longitude;
    private long time;
    private LocationRequest location_request;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST=9000;
    protected TextView txtv_today_date,txtv_temperature,txtv_max_temp,txtv_min_temp,txtv_humidity,txtv_place;
    protected ImageView imv_weather_icon;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtv_today_date=(TextView) findViewById(R.id.today_date);
        txtv_temperature=(TextView) findViewById(R.id.temperature);
        txtv_max_temp=(TextView) findViewById(R.id.temp_max);
        txtv_min_temp=(TextView) findViewById(R.id.temp_min);
        txtv_humidity=(TextView) findViewById(R.id.humidity);
        txtv_place=(TextView) findViewById(R.id.place);
        imv_weather_icon=(ImageView) findViewById(R.id.weather_icon);

        //set interval and fastest rate at which the location updates will be received in milliseconds
        location_request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000).setFastestInterval(5000);
        if (google_api_client.equals(null)) {
            google_api_client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                    .addApi(API).build();
        }
        if(user_location != null){
            lattitude=user_location.getLatitude();
            longitude=user_location.getLongitude();
            //create asynttask and pass the coordinates
            new JSONWeatherParse(lattitude,longitude,imv_weather_icon,txtv_today_date,txtv_temperature,txtv_max_temp,txtv_min_temp,txtv_humidity,txtv_place).execute();
        }
    }
    @Override
    public void onStart() {
        google_api_client.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        google_api_client.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(getApplicationContext(),"Please turn on your location settings",Toast.LENGTH_LONG).show();
            return;
        }
        user_location = FusedLocationApi.getLastLocation(google_api_client);
        if (user_location != null) {
            handleNewLocation(user_location);
        }
        else {
            LocationServices.FusedLocationApi.requestLocationUpdates(google_api_client,location_request,this);
        }

    }
    private void handleNewLocation(Location loc){
        lattitude = loc.getLatitude();
        longitude = loc.getLongitude();
        time=loc.getTime();
    }

    @Override
    public void onConnectionSuspended(int i) {

        Toast.makeText(getApplicationContext(), "Connection Suspended", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        //Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_LONG).show();
        if(connectionResult.hasResolution()){
            try{
                connectionResult.startResolutionForResult(this,CONNECTION_FAILURE_RESOLUTION_REQUEST);
            }catch (IntentSender.SendIntentException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
    @Override
    protected void onResume(){
        super.onResume();
        google_api_client.connect();
    }
    @Override
    protected void onPause(){
        super.onPause();
        if(google_api_client.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(google_api_client,this);
            google_api_client.disconnect();
        }
    }
}

