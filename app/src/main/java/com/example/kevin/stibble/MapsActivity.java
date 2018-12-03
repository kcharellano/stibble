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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextClock;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongToIntFunction;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

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
                //get all database messages
                /*NOTE: should be an asynctask*/
                stibbleMessage addedMessage = dataSnapshot.getValue(stibbleMessage.class);
                if(addedMessage!= null) {
                    String str = dataSnapshot.getKey();
                    addedMessage.setKey(str);
                    Log.d("temp", str);
                    LatLng mlatlng = new LatLng(addedMessage.getLatitude(), addedMessage.getLongtitude());
                    MarkerOptions options = new MarkerOptions()
                            .position(mlatlng)
                            .title(addedMessage.getTitle())
                            .snippet(addedMessage.getMessage());
                    mMap.addMarker(options).setTag(addedMessage);
                    Log.d(TAG, "onCreate:onChildAdded: Finish");
                }
                else
                {
                    Log.d(TAG, "onCreate:onChildAdded: something went wrong");

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("dbRef", "onChildChanges");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d("dbRef", "onChildRemoved");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("dbRef", "onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("dbRef", "onCancelled");
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
            mMap.setOnMarkerClickListener(this);
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

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final stibbleMessage markerStibble = (stibbleMessage) marker.getTag();
        if (markerStibble != null) {
            String title = markerStibble.getTitle();
            String message = markerStibble.getMessage();
            final Long longRating = markerStibble.getRating();
            String rating = longRating.toString();
            Toast.makeText(MapsActivity.this, "CLICK", Toast.LENGTH_SHORT).show();
            //create inflater for popup layout
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            if(inflater!=null) {
                //inflate the layout of the popup window
                final View ppView = inflater.inflate(R.layout.popup_window, null);
                //create the popup window
                //taps outside popup will close window
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                final PopupWindow ppWindow = new PopupWindow(ppView, width, height, true);
                //set title
                TextView ppTitle = (TextView) ppView.findViewById(R.id.popup_title);
                ppTitle.setText(title);
                //set message
                TextView ppMessage = (TextView) ppView.findViewById(R.id.popup_message);
                ppMessage.setText(message);
                //set rating
                final TextView ppRating = (TextView) ppView.findViewById(R.id.popup_rating);
                ppRating.setText(rating);
                //set buttons
                Button closePopup = (Button) ppView.findViewById(R.id.close_popup);
                Button incRating = (Button) ppView.findViewById(R.id.popup_uprating);
                Button decRating = (Button) ppView.findViewById(R.id.popup_downrating);
                //show the popup window
                ppWindow.showAtLocation(ppView, Gravity.CENTER, 0, 0);
                //button listeners
                closePopup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ppWindow.dismiss();
                    }
                });
                incRating.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //increment stibble object rating
                        markerStibble.incrementRating();
                        //update rating in database
                        mapsActivityDatabaseRef.child(markerStibble.getKey()).child("rating").setValue(markerStibble.getRating());
                        //update textview
                        Long L_rating = markerStibble.getRating();
                        String S_rating = L_rating.toString();
                        ppRating.setText(S_rating);
                        //update marker setTag() object
                        marker.setTag(markerStibble);
                    }
                });
                decRating.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //decrement stibble object rating
                        markerStibble.decrementRating();
                        //update rating in database
                        mapsActivityDatabaseRef.child(markerStibble.getKey()).child("rating").setValue(markerStibble.getRating());
                        //update textview
                        Long L_rating = markerStibble.getRating();
                        String S_rating = L_rating.toString();
                        ppRating.setText(S_rating);
                        //update marker setTag() object
                        marker.setTag(markerStibble);
                    }
                });
            }
        }
        return false;
    }

}
