package com.example.gpslimpio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class cords extends AppCompatActivity {

    TextView tvLongitud, tvAltitud;
    private FusedLocationProviderClient location;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cords);

        tvLongitud = (TextView) findViewById(R.id.longitudTV);
        tvAltitud = (TextView) findViewById(R.id.altitudTV);
        location = LocationServices.getFusedLocationProviderClient(this);
        button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
    }



    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                        1);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION},
                        2);
            }
            return;
        }
        location.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("quepasa", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                            Toast.makeText(getApplicationContext(),"Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                            tvAltitud.setText(String.valueOf( location.getAltitude()));
                            tvLongitud.setText(String.valueOf( location.getLongitude()));
                        }else{
                            Toast.makeText(getApplicationContext(),"Latitude: " + "no" + ", Longitude: " + "no", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}