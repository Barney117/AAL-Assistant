package com.example.aal_assistant;

public class locationCon {

    static double latitude;
    static double longitude;
    static String address;


    public locationCon() {
    }


    public locationCon(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public static double getLatitude() {
        return latitude;
    }

    public static double getLongitude() {
        return longitude;
    }

    public static String getAddress() {
        return address;
    }
}