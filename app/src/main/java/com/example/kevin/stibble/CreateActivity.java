package com.example.kevin.stibble;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class CreateActivity extends AppCompatActivity implements NoticeDialogFragment.NoticeDialogListener, GetLocationTaskResponse
{
    /*do some checking for placing a message with atleast the constructor requirements
     * check for location turned on when trying to place a message
      * don't get last known location; request current location
      *     if not found use last known location*/
    public boolean flag;
    public Location holdLocation = null;
    public final String TAG = "CreateActivity";
    DatabaseReference createActivityDatabaseRef;
    ChildEventListener createActivityDbListener;
    EditText wMessage, wTitle;
    TextView wMessageCount,wTitleCount;
    int wMessageCursorPos;
    int wMessageMaxLength, wTitleMaxLength;
    String limitLineString = "";
    //<-------------------------------------------------->
    //NOTE: INCLUDE VERSION CHECK FOR GMAP API (Coding with mitch)
    //Check for location permission
    //<-------------------------------------------------->

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        createActivityDatabaseRef = FirebaseDatabase.getInstance().getReference().getRoot().child("location"); /* */
        Log.d(TAG, "OnCreate: commence");

    //input filter to limit the amount of characters user can input in write message
        wMessage = (EditText) findViewById(R.id.et_writeMessage);
        wMessageMaxLength =1000;
        InputFilter[] wm_FilterArray = new InputFilter[1];
        wm_FilterArray[0] = new InputFilter.LengthFilter(wMessageMaxLength);
        wMessage.setFilters(wm_FilterArray);
     //input filter to limit the amount of characters use can input in write title
        wTitle = (EditText)findViewById(R.id.et_writeTitle);
        wTitleMaxLength = 50;
        InputFilter[] wt_FilterArray = new InputFilter[1];
        wt_FilterArray[0] = new InputFilter.LengthFilter(wTitleMaxLength);
        wTitle.setFilters(wt_FilterArray);

     //character count on message title
        wTitleCount = (TextView) findViewById(R.id.wt_input_count);
        wTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String stringvar2 = wTitle.getText().toString();
                int num2 = stringvar2.length();
                wTitleCount.setText(""+(int)num2);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

     //character count on message body & limit amount of lines user can input
        wMessageCount = (TextView) findViewById(R.id.wm_input_count);
        wMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                wMessageCursorPos = wMessage.getSelectionStart(); //limit lines
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //store and display character count in message body
                String stringVar = wMessage.getText().toString();
                int num = stringVar.length();
                wMessageCount.setText(""+(int)num);

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                //limit lines
                wMessage.removeTextChangedListener(this);
                if(wMessage.getLineCount() > 30)
                {
                    wMessage.setText(limitLineString);
                    wMessage.setSelection(wMessageCursorPos);
                }
                else
                {
                    limitLineString = wMessage.getText().toString();
                }
                wMessage.addTextChangedListener(this);
            }
        });

        createActivityDbListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //gets called whenever a new message is added to the list
                //also triggered when a child listener is detached
                /*temp off for demo*/
                //stibbleMessage addedMessage = dataSnapshot.getValue(stibbleMessage.class);
               // Toast.makeText(CreateActivity.this, addedMessage != null ? addedMessage.getTitle() : null, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(CreateActivity.this, "ERROR OCCURRED", Toast.LENGTH_SHORT).show();
            }
        };
        createActivityDatabaseRef.addChildEventListener(createActivityDbListener);


    }
    //onStart() gets device location on background thread
    @Override
    protected void onStart()
    {
        super.onStart();
        //gets Location on start of the activity
        getLoc();
    }
    //for toolbar
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_create, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //discard button on click (TOOLBAR ICON)
    public void discardMessage(MenuItem menuItem)
    {
        DialogFragment newFragment = new NoticeDialogFragment();
        newFragment.show(getFragmentManager(), "discard");
    }

    //done button on click (TOOLBAR ICON)
    public void saveMessage(MenuItem menuItem)
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),0);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        wMessage.setText("");
        wTitle.setText("");
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        return;
    }

    public void placeMessage(View view)
    {
        if(flag) {
            //create stibble object w/title, message, latitude, longitude
            stibbleMessage newMessage = new stibbleMessage(wTitle.getText().toString(),
                    wMessage.getText().toString(),
                    holdLocation.getLatitude(),
                    holdLocation.getLongitude());
            //initialize fields
            Geocoder geocoder = new Geocoder(this);
            try {
                List<Address> addressList = geocoder.getFromLocation(holdLocation.getLatitude(), holdLocation.getLongitude(), 1);
                Address holdAddress = addressList.get(0);
                newMessage.setAddress(holdAddress.getAddressLine(0));
                newMessage.setCity(holdAddress.getLocality());
                newMessage.setState(holdAddress.getAdminArea());
                newMessage.setCountry(holdAddress.getCountryName());
                newMessage.setPostalCode(holdAddress.getPostalCode());
                createActivityDatabaseRef.push().setValue(newMessage);
                Intent intent = new Intent(CreateActivity.this, HomeScreenActivity.class);
                startActivity(intent);
            } catch (IOException e) {
                Log.d(TAG, "placeMessage: something went wrong");
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show();
        }
    }
    //location from GetLocationTask is received here
    /*NOTE: Remove Geocoder for final version*/
    @Override
    public void processFinish(Location location) {
        holdLocation = location;
        if(holdLocation != null)
        {
            Geocoder geocoder = new Geocoder(this);
            try {
                List<Address> addressList = geocoder.getFromLocation(holdLocation.getLatitude(), holdLocation.getLongitude(), 1);
                Address holdAddress = addressList.get(0);
                flag = true;
                Log.d(TAG, "processFinish************");
                Toast.makeText(this, ""+holdAddress.toString(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.d(TAG, "processFinish: something went wrong");
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(this, "not5 success", Toast.LENGTH_SHORT).show();
        }
    }

    public void getLoc()
    {
        //starts as false
        //set to true once onProcessFinish() terminates
        flag = false;
        GetLocationTask locObj = new GetLocationTask();
        locObj.messenger = this;
        locObj.execute(this);
        Log.d(TAG, "getLoc2************");
    }
}


