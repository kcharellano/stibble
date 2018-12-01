package com.example.kevin.stibble;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //constants
    public final String TAG = "MapsActivity";
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    public static final float DEFAULT_ZOOM = 10;
    //variables
    public boolean myLocationPermissionGranted = false;
    private GoogleMap mMap;
    public FusedLocationProviderClient myFusedLocationProviderClient;
    DatabaseReference mapsActivityDatabaseRef;
    ChildEventListener mapsActivityDatabaseLis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapsActivityDatabaseRef = FirebaseDatabase.getInstance().getReference().getRoot().child("location");
        getLocationPermission();
        mapsActivityDatabaseLis = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onCreate:onChildAdded: start");
                //gets called whenever a new message is added to the list
                //also triggered when a child listener is detached
                /*temp off for demo*/
                //get all database messages
                /*NOTE: should be an asynctask*/
                stibbleMessage addedMessage = dataSnapshot.getValue(stibbleMessage.class);
                LatLng mlatlng = new LatLng(addedMessage != null ? addedMessage.getLatitude() : 0, addedMessage != null ? addedMessage.getLongtitude() : 0);
                MarkerOptions options = new MarkerOptions().position(mlatlng).title(addedMessage != null ? addedMessage.getTitle() : null).snippet(addedMessage != null ? addedMessage.getMessage() : null);
                mMap.addMarker(options);
                Log.d(TAG, "onCreate:onChildAdded: Finish");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    //permission checker
    //calls initMap()
    public void getLocationPermission()
    {
        Log.d(TAG, "getLocationPermission: start");
        //you always need to explicity check certain permissions
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                myLocationPermissionGranted = true;
                //requested permissions are granted so initialize map
                initMap();
            }
            else
            {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else
        {
            Toast.makeText(this, "here", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
        Log.d(TAG, "getLocationPermission: finish");
    }
    //initializes map
    public void initMap()
    {
        Log.d(TAG, "initMap: start");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d(TAG, "initMap: finish");
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    //called when map is ready--after initMap()
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: start");
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        if (myLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission
                            (this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
           mapsActivityDatabaseRef.addChildEventListener(mapsActivityDatabaseLis);
           mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Toast.makeText(MapsActivity.this, "CLICK", Toast.LENGTH_SHORT).show();
                    mMap.setInfoWindowAdapter(new CustomMessageWindowAdapter(MapsActivity.this));
                    return false;
                }
            });
        }
        Log.d(TAG, "onMapReady: finish");
    }
    //called if first time user opens map activity
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult: start");
        myLocationPermissionGranted = false;
        switch(requestCode)
        {
            case LOCATION_PERMISSION_REQUEST_CODE:
            {
                if(grantResults.length > 0)
                {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            //myLocationPermissionGranted will stay false
                            return;
                        }
                    }
                    myLocationPermissionGranted = true;
                    //initialize map
                    initMap();
                }
            }
        }
        Log.d(TAG, "onRequestPermissionResult: finish");
    }

    //gets device location and moves camera to current location
    public void getDeviceLocation()
    {
        Log.d(TAG, "getDeviceLocation: start");
        //get location
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try
        {
            if(myLocationPermissionGranted)
            {
                Task location = myFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful())
                        {
                            Log.d(TAG, "onComplete: found location");
                            Location curLocation = (Location) task.getResult();
                            //move camera to location
                            if (curLocation != null) {
                                moveCamera(new LatLng(curLocation.getLatitude(), curLocation.getLongitude()), DEFAULT_ZOOM);
                            }
                            else
                            {
                                Toast.makeText(MapsActivity.this, "Unable to get current location",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "Unable to get current location",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        catch(SecurityException e)
        {
            Log.e(TAG, "getDeviceLocation: SecurityException: "+ e.getMessage());
        }
        Log.d(TAG, "getDeviceLocation: finish");
    }

    //method to move camera to a specific location
    public void moveCamera(LatLng latLng, float zoom )
    {
        Log.d(TAG, "moveCamera: start");
        //add log.d message to notify where camera is moving to
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        Log.d(TAG, "moveCamera: finish");
    }
}
