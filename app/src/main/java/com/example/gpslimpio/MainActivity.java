package com.example.gpslimpio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btn1,btn2,btn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, 3);

        String[] permissions2 = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions2, 4);

//        String[] permissions3 = {Manifest.permission.MANAGE_EXTERNAL_STORAGE};
//        ActivityCompat.requestPermissions(this, permissions3, 20);



        btn1 = (Button) findViewById(R.id.buttonCords);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a  = new Intent(MainActivity.this, cords.class);
                startActivity(a);
            }
        });
        btn2 = (Button) findViewById(R.id.buttonAparcament);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a  = new Intent(MainActivity.this, aparcament.class);
                startActivity(a);
            }
        });

        btn3 = (Button) findViewById(R.id.buttonTrack);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(MainActivity.this, Track.class);
                startActivity(a);
            }
        });



        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {

            Toast.makeText(getApplicationContext(),"true",Toast.LENGTH_SHORT).show();

        }

        Toast.makeText(getApplicationContext(),"FALSE",Toast.LENGTH_SHORT).show();

    }






    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 7:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido, puedes acceder al almacenamiento externo
                } else {
                    // Permiso denegado, no puedes acceder al almacenamiento externo
                }
                return;
        }
    }

}