package com.example.speed;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.TextView;
//import android.support.annotation.Nullable;

import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private static final long INTERVAL = 1000 * 2;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation, lStart, lEnd;
    static double distance = 0;
    double speed;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("value");

    String Value;

    double longitude, latitude;

//    static TextView longitude, latitude;


    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return mBinder;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
        }
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        distance = 0;
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(Location location) {
        MainActivity.locate.dismiss();
        mCurrentLocation = location;

        longitude = location.getLongitude();
        latitude = location.getLatitude();

        System.out.println(longitude);
        System.out.println(latitude);


        if (lStart == null) {
            lStart = mCurrentLocation;
            lEnd = mCurrentLocation;
        } else
            lEnd = mCurrentLocation;

        //Calling the method below updates the  live values of distance and speed to the TextViews.
        updateUI(longitude, latitude);
        //calculating the speed with getSpeed method it returns speed in m/s so we are converting it into kmph
        speed = location.getSpeed() * 18 / 5;

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }


    }

    //The live feed of Distance and Speed are being set in the method below .
    private void updateUI(double longitude, double latitude) {
        if (MainActivity.p == 0) {
            distance = distance + (lStart.distanceTo(lEnd));
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toSeconds(diff);
            MainActivity.time.setText("Total Time: " + diff + " minutes");
            MainActivity.latitude.setText(Double.toString(latitude));
            MainActivity.longitude.setText(Double.toString(longitude));
            if (speed > 0.0)
                MainActivity.speed.setText("Current speed: " + new DecimalFormat("#.##").format(speed) + " m/s");
            else
                MainActivity.speed.setText(".......");

            MainActivity.dist.setText(new DecimalFormat("#.###").format(distance) + " m's.");

            lStart = lEnd;

            speed = Math.round(speed * 100.0) / 100.0;

            Value = "0"+"1"+Double.toString(latitude).substring(0,8) + Double.toString(longitude).substring(0,9)+ speed;

            MainActivity.valueString.setText(Value);
            myRef.setValue(Value);

        }

    }


    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }
}
