package com.example.exerciseuserlocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView latitude;
    private TextView longitude;
    private TextView accuracy;
    private TextView address;
    private TextView altitude;

    private static final String TAG ="MainActivity";
    public static final int REQUEST_CODE = 1;
    public static final int UPDATE_INTERVAL = 5000;
    public static final int FASTEST_INTERVAL = 3000;

    //step -3
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private List<String> permissionsToRequest;
    private List<String> permissions = new ArrayList<>();
    private List<String> permissionsRejected = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitude = findViewById(R.id.lattitude);
        longitude = findViewById(R.id.longitude);
        accuracy = findViewById(R.id.accuracy);
        altitude = findViewById(R.id.altitude);
        address = findViewById(R.id.address);


        // step - 4 instantiate the fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // add permissions
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        permissionsToRequest = permissionsToRequest(permissions);
        if (permissionsToRequest.size() > 0) {
            requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), REQUEST_CODE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop: ");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private List<String> permissionsToRequest(List<String> permissions) {
        ArrayList<String> results = new ArrayList<>();
        for (String perm : permissions) {
            if (!hasPermission(perm))
                results.add(perm);
        }

        return results;
    }

    private boolean hasPermission(String perm) {
        return checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // this is a proper place to check the google play services availability

        int errorCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, errorCode, errorCode, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Toast.makeText(MainActivity.this, "No Services", Toast.LENGTH_SHORT).show();
                        }
                    });
            errorDialog.show();
        } else {
            Log.i(TAG, "onPostResume: ");
            findLocation();
        }
    }

    private void findLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        //initializing geocoder
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                        try {
                            List<Address> fromLocation = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            latitude.setText(String.format("%s", location.getLatitude()));
                            longitude.setText(String.format("%s", location.getLongitude()));
                            altitude.setText(String.format("%s", location.getAltitude()));
                            accuracy.setText(String.format("%s", location.getAccuracy()));
                            address.setText(String.format("%s", fromLocation.get(0).getCountryName(),fromLocation.get(0).getAdminArea(),
                                    fromLocation.get(0).getSubAdminArea(),fromLocation.get(0).getPostalCode()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            });
        }

        startUpdateLocation();
    }

    @SuppressLint("MissingPermission")
    private void startUpdateLocation() {
        Log.d(TAG, "startUpdateLocation: ");
        // location request
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                try {
                    List<Address> fromLocation = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    latitude.setText(String.format("%s", location.getLatitude()));
                    longitude.setText(String.format("%s", location.getLongitude()));
                    altitude.setText(String.format("%s", location.getAltitude()));
                    accuracy.setText(String.format("%s", location.getAccuracy()));
                    address.setText(String.format("%s", fromLocation.get(0).getCountryName(),fromLocation.get(0).getAdminArea(),
                            fromLocation.get(0).getSubAdminArea(),fromLocation.get(0).getPostalCode()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            for (String perm: permissions) {
                if (!hasPermission(perm))
                    permissionsRejected.add(perm);
            }

            if (permissionsRejected.size() > 0 ) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("The location permission is mandatory")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), REQUEST_CODE);
                                    }

                                }
                            }).setNegativeButton("Cancel", null)
                            .create()
                            .show();
                }
            }
        }
    }
}
