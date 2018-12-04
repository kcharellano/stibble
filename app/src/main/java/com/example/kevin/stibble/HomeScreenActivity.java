package com.example.kevin.stibble;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeScreenActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    Button createBtn; //declare create button globally
    DatabaseReference HomeActivityDatabaseRef; /* */
    ChildEventListener HomeActivityDbListener;
    Button findBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        HomeActivityDatabaseRef = FirebaseDatabase.getInstance().getReference(); /* */
        if (isServicesOk()) {
            findInit();
        }
        createBtn = (Button) findViewById(R.id.create_button); //instantiate & bind to xml button
        createBtn.setOnClickListener(new View.OnClickListener() //setOnClickListener
        {
            @Override
            public void onClick(View v)   //runs on click
            {

                Intent gotoCreateActivity = new Intent(HomeScreenActivity.this, CreateActivity.class);
                startActivity(gotoCreateActivity);
            }
        });

        HomeActivityDbListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //gets called whenever a new message is added to the list
                //also triggered when a child listener is detached
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //called when contents of a child are changed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //called when child is deleted
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //called if child changes position
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //called when an error occurs when trying to make changes
            }
        };
        HomeActivityDatabaseRef.addChildEventListener(HomeActivityDbListener);
        Long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("dd/MM/yy", Locale.US);
        String str = dayTime.format(new Date(time));
        String str2 = time.toString();
        Log.d("home", str);
        Log.d("home", str2);

    }

    //method for checking correct gmap version
    public boolean isServicesOk()
    {
        Log.d(TAG, "isServicesOk: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(HomeScreenActivity.this);

        if(available == ConnectionResult.SUCCESS)
        {
            //everything is fine and the user can make map requests
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            //we can resolve the versioning issue
            Log.d(TAG, "isServicesOk: error occurred but it is fixable");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(HomeScreenActivity.this, available, ERROR_DIALOG_REQUEST);

        }
        else
        {
            //nothing we can do :(
            Toast.makeText(this, "YOU CANNOT MAKE MAP REQUESTS", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "FIND-Button will not take you to map", Toast.LENGTH_SHORT).show();

        }
        return false;
    }

    //initialize find button if isServicesOk returns true
    public void findInit()
    {
        findBtn = (Button) findViewById(R.id.find_button);
        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent anotherIntent = new Intent(HomeScreenActivity.this, MapsActivity.class);
                startActivity(anotherIntent);
            }
        });
    }

}
