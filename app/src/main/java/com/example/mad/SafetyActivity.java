package com.example.mad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class SafetyActivity extends AppCompatActivity {

    private TextView tvLocation;
    private Button btnSendAlert, btnCallSecurity, btnMap, btnBack; // Make sure btnShowMap is here!
    private FusedLocationProviderClient fusedLocationClient;

    // Variables to store coordinates for the map intent
    private double currentLat = 0;
    private double currentLon = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safety);

        // Now these lines will turn from red to purple/white
        tvLocation = findViewById(R.id.tvLocation);
        btnSendAlert = findViewById(R.id.btnSendAlert);
        btnCallSecurity = findViewById(R.id.btnCallSecurity);
        btnMap = findViewById(R.id.btnMap);
        btnBack = findViewById(R.id.btnBack);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //GPS Sensor: Fetch Location
        btnSendAlert.setOnClickListener(v -> checkPermissionAndGetLocation());

        //Implicit Intent: Open Google Maps
        btnMap.setOnClickListener(v -> openMap(currentLat, currentLon));

        //Implicit Intent: Dial Security
        btnCallSecurity.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:999"));
            startActivity(intent);
        });

        //Smart Back Button Logic
        btnBack.setOnClickListener(v -> {
            finish(); // Closes this activity and returns to HomeActivity
        });
    }

    private void checkPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            fetchLocation();
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLon = location.getLongitude();

                    tvLocation.setText("Location: " + currentLat + ", " + currentLon);

                    // Show the Map button once coordinates are valid
                    btnMap.setVisibility(View.VISIBLE);

                    Toast.makeText(SafetyActivity.this, "Location Secured", Toast.LENGTH_SHORT).show();
                } else {
                    // Triggered if GPS is disabled or no signal
                    Toast.makeText(SafetyActivity.this, "Cannot find location. Enable GPS/Location Settings.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }
        });
    }

    private void openMap(double lat, double lon) {
        // geo:lat,lon?q=lat,lon(Label) creates a pin on the map
        String uri = "geo:" + lat + "," + lon + "?q=" + lat + "," + lon + "(Current Location)";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Fallback to browser if Maps app isn't installed
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + lat + "," + lon);
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            Toast.makeText(this, "Permission Denied! Cannot access GPS.", Toast.LENGTH_SHORT).show();
        }
    }
}