package com.example.gpslimpio;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.util.Xml;
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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Track extends AppCompatActivity implements LocationListener, GpsStatus.Listener,OnMapReadyCallback,TaskLoadedCallback{

    Button startStop,importgpx;
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
    private static final int PICK_PDF_FILE = 2;
    ArrayList<LatLng> points = new ArrayList<>();
    Polyline line;
    private static final int PICK_XML_FILE = 1;
    ArrayList<LatLng> ubicacionsImport= new ArrayList<>();
    ArrayList<LatLng> ubicacions= new ArrayList<>();
    ArrayList<MarkerOptions> marcadors = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable tiempo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);


        startStop = (Button) findViewById(R.id.button2);
        importgpx = (Button) findViewById(R.id.importgpx);
        importgpx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 345);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] mimeTypes = {"application/gpx+xml", "application/xml","text/xml"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, PICK_XML_FILE);



//                // Crea un intent para seleccionar un archivo GPX
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("*/*");
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                String[] mimeTypes = {"application/gpx+xml", "application/xml"};
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
//
//// Agrega el filtro de proveedor de documentos
//                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//
//// Inicia la actividad para seleccionar el archivo GPX
//                startActivityForResult(intent, 333);


//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("application/gpx+xml");
//                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 333);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }
            }

        });

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
                    mMap.clear();
                    showLocation();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            points = new ArrayList<>();
                        LatLng principiR= new LatLng(Double.valueOf(String.valueOf(latitude_tv.getText())), Double.valueOf(String.valueOf(longitude_tv.getText())));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(principiR,15));
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(principiR);
                        markerOptions.title("inici");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        mMap.addMarker(markerOptions);
                        tracking=true;
                        startStop.setText("STOP");
                            startStopp=false;
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
                    startStop.setText("START");
                    try {
                        writeFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    tracking=false;
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

    public void writeFile() throws IOException {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 500);
            return;
        }


        File folder =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);


        File file= new File(folder,"ruta.gpx");
        FileOutputStream fos = new FileOutputStream(file);
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(fos,"UTF-8");

        serializer.startDocument("UTF-8",Boolean.valueOf(true));

        serializer.startTag(null,"gpx");
        serializer.attribute(null,"version","1.1");
        serializer.attribute(null,"appgps","APP");


        serializer.startTag(null,"trk");
        serializer.startTag(null,"rutaApp");
        serializer.text("Mi pista");
        serializer.endTag(null,"rutaApp");


        serializer.startTag(null, "trkseg");

        for (int i = 0; i < ubicacions.size(); i++) {
            serializer.startTag(null,"trkpt");
            serializer.attribute(null, "lat",String.valueOf(ubicacions.get(i).latitude));
            serializer.attribute(null, "lon",String.valueOf(ubicacions.get(i).longitude));
            serializer.endTag(null,"trkpt");
        }

        serializer.endTag(null,"trkseg");

        serializer.endTag(null,"trk");
        serializer.endTag(null,"gpx");

        serializer.endDocument();

        fos.close();
        Toast.makeText(getApplicationContext(), "route saved", Toast.LENGTH_SHORT).show();


    }


    public void readFile(Uri uri) throws IOException, XmlPullParserException {

        Toast.makeText(getApplicationContext(),"asdasdasdasda", Toast.LENGTH_SHORT).show();

        InputStream inputStream = getContentResolver().openInputStream(uri);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(inputStream,null);


        while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equals("trkpt")) {
                Double lat = Double.valueOf(xpp.getAttributeValue(null, "lat"));
                Double lon = Double.valueOf(xpp.getAttributeValue(null, "lon"));

                ubicacionsImport.add(new LatLng(lat,lon));
            }
            xpp.next();
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionsImport.get(1),15));


        inputStream.close();

        drawImportLine();
    }

    public void drawImportLine(){
            mMap.clear();
            int middle= ubicacionsImport.size()/2;

        PolylineOptions options2 = new PolylineOptions().width(5).color(Color.RED).geodesic(true);
        for (int i = 0; i < ubicacionsImport.size(); i++) {
            if (i==1){
                MarkerOptions markerOptions3 = new MarkerOptions();
                markerOptions3.position(ubicacionsImport.get(i));
                markerOptions3.title("Principi");
                markerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                mMap.addMarker(markerOptions3);
            }else if (i==middle){
                MarkerOptions markerOptions3 = new MarkerOptions();
                markerOptions3.position(ubicacionsImport.get(i));
                markerOptions3.title("Mitja ruta");
                markerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                mMap.addMarker(markerOptions3);
            }else if (i==ubicacionsImport.size()-2){
                MarkerOptions markerOptions3 = new MarkerOptions();
                markerOptions3.position(ubicacionsImport.get(i));
                markerOptions3.title("Final");
                markerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                mMap.addMarker(markerOptions3);
            }
            LatLng point = ubicacionsImport.get(i);
            options2.add(point);
        }

        line = mMap.addPolyline(options2);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getApplicationContext(),"actresult", Toast.LENGTH_SHORT).show();

        if (requestCode == PICK_XML_FILE && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            try {
                readFile(uri);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }

        }
    }
}