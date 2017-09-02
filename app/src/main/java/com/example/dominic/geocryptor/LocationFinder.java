package com.example.dominic.geocryptor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Dominic on 8/28/2017.
 */

public class LocationFinder extends Activity{

    public static final String[] LOCATION_PERM={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final int LOCATION_REQUEST = 1425;


    LocationManager locationManager;
    LocationListener locationListener;
    Location currentBestLoc;
    Context context;
    Activity activity;

    boolean gpsEnabled;

    public LocationFinder(Activity act, Context con){
        context = con;
        activity = act;
        locationManager = (LocationManager) con.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentBestLoc = location;
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
    }

    public void updateContext(Activity act, Context con){
        context = con;
        activity = act;
    }

    //This code combines the latitude and longitude into one string for the encryption key generation
    //It also rounds the latitude and longitude to 4 digits (around 10 meter square)
    public String getLocationKey(){
        String formatLat = Math.round(getLocation().getLatitude() * 10000) / 10000. + "";
        String formatLong = Math.round(getLocation().getLongitude() * 10000) / 10000. + "";
        return formatLat + ", " + formatLong;
    }

    //Returns true if GPS was enabled and location was started
    public boolean startUpdatingLocation(){
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!gpsEnabled){
            showGPSAlert();
            return false;
        }
        else {
            try {
                if (ContextCompat.checkSelfPermission(context, LOCATION_PERM[0]) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    System.out.println("location logging starting");
                    currentBestLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    return true;
                }
            } catch (SecurityException e) {
                System.out.print(e.getMessage());
            }
            return true;
        }
    }

    public void showGPSAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //Set message and title
        builder.setMessage("GeoCryptor needs GPS enabled in order to run successfully. Enable GPS to continue.");
        builder.setTitle("Location Missing");

        //Add button
        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //When they click OK bring to location settings
                            attemptEnableGPS();
                    }
                });

        //create and show alert
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void stopUpdatingLocation(){
        locationManager.removeUpdates(locationListener);
    }

    public Location getLocation(){
        return currentBestLoc;
    }

    private void attemptEnableGPS(){
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        try {
            activity.startActivity(intent);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("INTENT DIDNT SEND");
        }
    }

    public void restart(){
        stopUpdatingLocation();
        startUpdatingLocation();
    }

    public boolean isGPSEnabled(){
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
