package com.example.kevin.stibble;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import com.arsy.maps_library.MapRipple;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    //constants
    public final String TAG = "MapsActivity";
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    public static final float DEFAULT_ZOOM = 18.85f;
    //variables
    boolean mapRippleFlag;
    MapRipple mapRipple;
    public boolean myLocationPermissionGranted = false;
    public Location holdLocation;
    private GoogleMap mMap;
    private LinkedList<Marker> markerLinkedList;
    public  LocationRequest locationRequest;
    public FusedLocationProviderClient myFusedLocationProviderClient;
    LocationCallback locationCallback;
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

                    LatLng mlatlng = new LatLng(addedMessage.getLatitude(), addedMessage.getLongtitude());
                    //make marker invisible
                    MarkerOptions options = new MarkerOptions().visible(false).position(mlatlng);
                    //create marker object
                    Marker marker = mMap.addMarker(options);
                    //add object to marker
                    marker.setTag(addedMessage);
                    //appends to markerLinkedList
                    markerLinkedList.add(marker);
                    //mMap.addMarker(options).setTag(addedMessage);
                    Log.d(TAG, "onCreate:onChildAdded: Finish");
                }
                else
                {
                    Log.d(TAG, "onCreate:onChildAdded: something went wrong");

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("luk", "onChildChanges");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d("luk", "onChildRemoved");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("luk", "onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("dbRef", "onCancelled");
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapRippleFlag = false;
        markerLinkedList = new LinkedList<>();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(7000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(5000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d("Loc2", "updated");
                holdLocation = locationResult.getLastLocation();
                //String k = "latitude = "+holdLocation.getLatitude()+"\n longitude = "+holdLocation.getLongitude();
                //Toast.makeText(MapsActivity.this, k, Toast.LENGTH_SHORT).show();
                for (Marker marker : markerLinkedList) {
                    stibbleMessage marker_stib = (stibbleMessage) marker.getTag();
                    if(marker_stib!=null) {
                        float[] results = new float[1];
                        Location.distanceBetween(holdLocation.getLatitude(), holdLocation.getLongitude(), marker_stib.getLatitude(), marker_stib.getLongtitude(), results);
                        float distance = results[0];
                        if(distance < 70)
                        {
                            marker.setVisible(true);
                        }
                        else
                        {
                            marker.setVisible(false);
                        }
                        Log.d("distance", String.valueOf(distance));
                    }
                }
                if(!mapRippleFlag)
                {
                    LatLng latLng = new LatLng(holdLocation.getLatitude(), holdLocation.getLongitude());
                    mapRipple = new MapRipple(mMap, latLng, MapsActivity.this);
                    mapRipple.withNumberOfRipples(3);
                    mapRipple.withFillColor(ContextCompat.getColor(MapsActivity.this, R.color.darkGreen));
                    mapRipple.withStrokeColor(Color.BLACK);
                    mapRipple.withStrokewidth(0);
                    mapRipple.withDistance(60);
                    mapRipple.withRippleDuration(6000);
                    mapRipple.withTransparency(0.3f);
                    mapRipple.startRippleMapAnimation();
                    mapRippleFlag = true;
                }
                mapRipple.withLatLng(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()));
            }
        };
    }
    @Override
    protected void onStop() {
        super.onStop();
        myFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        mapRipple.stopRippleMapAnimation();
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
            myFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            Log.d("Loc2", "requested");
            mapsActivityDatabaseRef.addChildEventListener(mapsActivityDatabaseLis);
            //mMap.setMinZoomPreference(18.5f);
            //mMap.setMaxZoomPreference(18.0f);
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
        mapRipple.stopRippleMapAnimation();
        final stibbleMessage markerStibble = (stibbleMessage) marker.getTag();
        if (markerStibble != null) {
            String title = markerStibble.getTitle();
            String message = markerStibble.getMessage();
            final Long longRating = markerStibble.getRating();
            String rating = longRating.toString();
            Long expEpoch = markerStibble.getExpireEpoch();
            SimpleDateFormat dayTime = new SimpleDateFormat("MM/dd/yy", Locale.US);
            String expireEpoch = dayTime.format(new Date(expEpoch));
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
                //set expire date
                TextView ppExpire = (TextView) ppView.findViewById(R.id.popup_expire);
                ppExpire.setText(expireEpoch);
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
                        mapRipple.startRippleMapAnimation();
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


    public void showLocation(View view) {
        double lat = holdLocation.getLatitude();
        double lon = holdLocation.getLongitude();
        Toast.makeText(this, "latitude = "+lat+"\n"+"longitude = "+lon, Toast.LENGTH_LONG).show();
    }

}
