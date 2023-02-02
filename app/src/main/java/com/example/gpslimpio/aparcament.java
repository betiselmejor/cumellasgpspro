package com.example.gpslimpio;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class aparcament extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    GoogleMap mMap;
    private FusedLocationProviderClient location;
    Button btn;
    LatLng aparcament = new LatLng(2,2);

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aparcament);
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        requestPermissions(perms, 0);

        btn = (Button) findViewById(R.id.button);
        location = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMapClickListener(this);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            getLocation();

                try {
                    Log.d("quepasaa2", getPoblacio(aparcament));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Log.d("quepasaa3", getAdresa(aparcament));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public String getPoblacio(LatLng a) throws IOException {

        Geocoder geocoder = new Geocoder(aparcament.this, Locale.getDefault());
        List<Address>addressList = geocoder.getFromLocation(a.latitude,a.longitude,1);

        if (addressList.size()>0){
            String poblacio = String.valueOf(addressList.get(0).getLocality());
            return poblacio;
        }

    return "try again";

    }
    public String getAdresa(LatLng a) throws IOException {

        Geocoder geocoder = new Geocoder(aparcament.this, Locale.getDefault());
        List<Address>addressList = geocoder.getFromLocation(a.latitude,a.longitude,1);

        if (addressList.size()>0){
            String poblacio = String.valueOf(addressList.get(0).getAddressLine(0));
            return poblacio;
        }

    return "try again";

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
                            aparcament= new LatLng( location.getLatitude(),location.getLongitude());

                        }else{
                            Toast.makeText(getApplicationContext(),"Latitude: " + "no" + ", Longitude: " + "no", Toast.LENGTH_SHORT).show();

                        }
                    }
                });


    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        Toast.makeText(getApplicationContext(),"Latitude: " + latLng.latitude+ ", Longitude: " + latLng.longitude, Toast.LENGTH_LONG).show();

    }
}