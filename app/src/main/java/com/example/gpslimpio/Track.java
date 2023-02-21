package com.example.gpslimpio;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import android.Manifest;
import java.io.IOException;
import java.util.ArrayList;

public class Track extends AppCompatActivity implements LocationListener, GpsStatus.Listener,OnMapReadyCallback,TaskLoadedCallback{

    Button startStop,setMarkers;
    Polyline currentp;
    private FusedLocationProviderClient location;
    GoogleMap mMap;
    public LocationManager locationManager;
    TextView location_tv,longitude_tv,latitude_tv;
    LatLng inici = new LatLng(2,2);
    LatLng finaaal= new LatLng(2,2);
    public static int numAct=0;
    boolean tracking = false;
    boolean startStopp=false;
    private GoogleApiClient mGoogleApiClient;
    ArrayList<LatLng> points = new ArrayList<>();
    Polyline line;
    ArrayList<LatLng> ubicacions= new ArrayList<>();
    ArrayList<MarkerOptions> marcadors = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable tiempo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);


        startStop = (Button) findViewById(R.id.button2);
        setMarkers = (Button) findViewById(R.id.button3);

        location = LocationServices.getFusedLocationProviderClient(this);

        location_tv = (TextView) findViewById(R.id.locationTV);

        longitude_tv = (TextView) findViewById(R.id.textView5);
        latitude_tv = (TextView) findViewById(R.id.textView7);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        getLocation();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 10){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showLocation();
            }else{
                Toast.makeText(getApplicationContext(), "Permission not granted ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void showLocation(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
        location_tv.setText("Loading Location");
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        }else{
            Toast.makeText(getApplicationContext(), "ENABLE GPS!" , Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation(){
        location.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("quepasa", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                            Toast.makeText(getApplicationContext(),"Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                            inici= new LatLng( location.getLatitude(),location.getLongitude());


                        }else{
                            Toast.makeText(getApplicationContext(),"Latitude: " + "no" + ", Longitude: " + "no", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }



    private void hereLocation(Location location){
        location_tv.setText("LAT: " + location.getLatitude() + " LONG: " + location.getLongitude());
        longitude_tv.setText(String.valueOf(location.getLongitude()));
        latitude_tv.setText(String.valueOf(location.getLatitude()));
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (startStopp==false){
            hereLocation(location);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);
            ubicacions.add(latLng);
            Log.d("array","LAT: " + latitude + " LONG: " + longitude + "size: " + ubicacions.size());


            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

            points.add(latLng);
            redrawLine();
        }



    }
    private void redrawLine(){



        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }

        line = mMap.addPolyline(options); //add Polyline

    }




    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;



        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
                }else if (tracking==false){
                    showLocation();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                        LatLng principiR= new LatLng(Double.valueOf(String.valueOf(latitude_tv.getText())), Double.valueOf(String.valueOf(longitude_tv.getText())));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(principiR,15));
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(principiR);
                        markerOptions.title("inici");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        mMap.addMarker(markerOptions);
                        tracking=true;
                        startStop.setText("STOP");
                        }
                    },200);

                }else if (tracking==true){
                    MarkerOptions markerOptions2 = new MarkerOptions();
                    LatLng finalR= new LatLng(Double.valueOf(String.valueOf(latitude_tv.getText())), Double.valueOf(String.valueOf(longitude_tv.getText())));
                    markerOptions2.position(finalR);
                    markerOptions2.title("final");
                    mMap.addMarker(markerOptions2);
                    startStopp=true;
                    locationManager=null;
                    location=null;
                }
            }
        });

//        setMarkers.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (tracking==false &&startAndEnd == false){
//                    getLocation();
//                    inicii= new LatLng(Double.valueOf(String.valueOf(latitude_tv.getText())), Double.valueOf(String.valueOf(longitude_tv.getText())));
//
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(inicii);
//                    markerOptions.title("inici");
//                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//                    mMap.clear();
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(inicii,15));
//                    mMap.addMarker(markerOptions);
//                    tracking = true;
//                    setMarkers.setText("set final");
//                    marcadors.add(markerOptions);
//                }else if (tracking==true&&startAndEnd == false){
//                    getLocation();
//                    finaaal= new LatLng(Double.valueOf(String.valueOf(latitude_tv.getText())), Double.valueOf(String.valueOf(longitude_tv.getText())));
//
//                    MarkerOptions markerOptions2 = new MarkerOptions();
//                    markerOptions2.position(finaaal);
//                    markerOptions2.title("final");
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(finaaal,15));
//                    mMap.addMarker(markerOptions2);
//                    marcadors.add(markerOptions2);
//                    startAndEnd=true;
//                }else if (tracking==true &&startAndEnd == true){
//                    currentp = mMap.addPolyline(new PolylineOptions().add(finaaal,inicii));
//                    currentp.setVisible(true);
//                }
////
//
//            }
//        });

    }




    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onTaskDone(Object... values) {
        if (currentp != null){
            currentp.remove();
        }
        currentp= mMap.addPolyline((PolylineOptions) values[0]);
    }
}