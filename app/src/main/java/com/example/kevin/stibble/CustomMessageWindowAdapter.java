package com.example.kevin.stibble;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class CustomMessageWindowAdapter implements GoogleMap.InfoWindowAdapter{

    private final View myWindow;

    public  CustomMessageWindowAdapter(Context context)
    {
        myWindow = LayoutInflater.from(context).inflate(R.layout.stibble_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        rendoWindoText(marker, myWindow);
        return myWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendoWindoText(marker, myWindow);
        return myWindow;
    }
    private void rendoWindoText(Marker marker, View view)
    {
        String title = marker.getTitle();
        TextView tvTitle = (TextView) view.findViewById(R.id.stibbleTitle);
        if(!title.equals(""))
        {
            tvTitle.setText(title);
        }
        String snippet = marker.getSnippet();
        TextView tvSnippet = (TextView) view.findViewById(R.id.stibbleSnippet);
        if(!title.equals(""))
        {
            tvSnippet.setText(snippet);
        }
        stibbleMessage temp = (stibbleMessage)marker.getTag();
        int rating = (int) (temp != null ? temp.getRating() : 0);
        TextView tvRating = (TextView) view.findViewById(R.id.stibblRating);
        tvRating.setText(""+rating);
    }
}
