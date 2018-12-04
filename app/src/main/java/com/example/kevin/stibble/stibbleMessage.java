package com.example.kevin.stibble;

import android.location.Location;

public class stibbleMessage
{
    final private long ONE_WEEK = 604000000;
    private String title, message, key;
    private String address, city, state, country, postalCode;
    private double latitude, longitude;
    private long rating;
    private long placeEpoch, expireEpoch;

    //constructorS
    public stibbleMessage(String t, String m, double lat, double lng)
    {
        this.title = t;
        this.message = m;
        this.latitude = lat;
        this.longitude = lng;
        this.rating = 0;
        this.placeEpoch = 0;
        this.expireEpoch = 0;
    }
    public stibbleMessage()
    {
        //default constructor required for calls for some bullshit
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public long getRating() {
        return rating;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longitude;
    }

    public void setLongtitude(double longtitude) {
        this.longitude = longtitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public void incrementRating()
    {
        this.rating = this.rating + 1;
    }
    public void decrementRating()
    {
        this.rating = this.rating - 1;
    }

    public long getPlaceEpoch() {
        return placeEpoch;
    }

    public void setPlaceEpoch(long placeEpoch) {
        this.placeEpoch = placeEpoch;
    }

    public long getExpireEpoch() {
        return expireEpoch;
    }

    public void setExpireEpoch() {
        this.expireEpoch = this.placeEpoch + ONE_WEEK;
    }
}
