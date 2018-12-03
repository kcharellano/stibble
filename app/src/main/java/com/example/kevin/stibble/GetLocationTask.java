package com.example.kevin.stibble;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class GetLocationTask extends AsyncTask<Activity, Void, Void> {
    //needed to send info outside of this worker thread
    public GetLocationTaskResponse messenger = null;
    //holds location
    public Location holdLocation = null;
    //constants
    public static final String TAG = "GetLocationTask";
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    //other vars
    public boolean myLocationPermissionGranted = false;
    public FusedLocationProviderClient myFusedLocationProviderClient;

    @Override
    //pass in activity that called this task
    protected Void doInBackground(Activity... activities) {
        Log.d(TAG, "doInBackground: start");
        //set activity and context
        Activity activity = activities[0];
        final Context ctx = activity.getBaseContext();
        //get location permission
        getLocationPermission(ctx, activity);
        Log.d(TAG, "doInBackground: about to run returnDeviceAddress()");
        //gets device location from inner class via callback
        returnDeviceAddress(ctx, new LocationCallback() {
            @Override
            public void onLocation(Location location) {
                Log.d(TAG, "onLocation: callback");
                holdLocation = location;
                //sends location to parent activity via GetLocationTaskResponse interface
                messenger.processFinish(holdLocation);
            }
        });
        return null;
    }



    private void returnDeviceAddress(final Context context, final LocationCallback callback)
    {
        //get location
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        try {
            if (myLocationPermissionGranted) {
                //get last known location
                Task location = myFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                                                   @Override
                                                   public void onComplete(@NonNull Task task) {
                                                       if (task.isSuccessful()) {
                                                           Location curLocation = (Location) task.getResult();
                                                           if (curLocation != null) {
                                                               Log.d(TAG, "returnDeviceAddress: onComplete: starting callback");
                                                               callback.onLocation(curLocation);
                                                           } else {
                                                               Log.d(TAG, "returnDeviceAddress: onComplete: current location is null");
                                                           }
                                                       } else {
                                                           Log.d(TAG, "returnDeviceAddress onComplete: current location is null");

                                                       }
                                                   }
                                               }
                );
            }
        } catch (SecurityException e) {
            Log.e(TAG, "returnDeviceAddress: SecurityException: " + e.getMessage());
        }
    }
    public void getLocationPermission(Context context, Activity act)
    {
        Log.d(TAG, "getLocationPermission: start");
        //you always need to explicity check certain permissions
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(context.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(context.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                //here is everything goes good
                Log.d(TAG, "getLocationPermission: good to go");
                myLocationPermissionGranted = true;
            }
            else
            {
                ActivityCompat.requestPermissions(act, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else
        {
            ActivityCompat.requestPermissions(act, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
}
