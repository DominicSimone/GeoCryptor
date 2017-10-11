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


/**
 * Created by Dominic on 8/28/2017.
 *
 * This method finds the location of the user.
 *
 * It also builds the location key used to encrypt and decrypt data
 */

public class LocationFinder extends Activity{

    public static final String[] LOCATION_PERM={
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final int LOCATION_REQUEST = 1425;

    //Digits to round to (10^locationScale)
    final private static int locationScale = 3;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentBestLoc;
    private Context context;
    private Activity activity;

    private boolean gpsEnabled;

    /**
     * Constructs a LocationFinder object.
     * Builds a LocationManager and LocationListener in the activity and context passed in.
     * @param act the activity this method is used in
     * @param con the context this method is used in
     */
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

    /**
     * Used to update the context of the this object (if necessary)
     * @param act the new activity
     * @param con the new context
     */
    public void updateContext(Activity act, Context con){
        context = con;
        activity = act;
    }


    /**
     * This combines the latitude and longitude into one string for the encryption key generation
     * It also rounds the latitude and longitude to 4 digits (around 10 sq. meters)
     * @return locKey the location key string
     */
    public String getLocationKey(){
        int digits = (int) Math.pow(10, locationScale);
        String formatLat = Math.round(getLocation().getLatitude() * digits) / digits + "";
        String formatLong = Math.round(getLocation().getLongitude() * digits) / digits + "";
        return formatLat + ", " + formatLong;
    }

    //Returns true if GPS was enabled and location was started

    /**
     * Attempts to start location finding
     * @return boolean if GPS was enabled and location was started
     */
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

    /**
     * If GPS is not enabled, this method shows an alert box informing the user
     * "GeoCryptor needs GPS enabled in order to run successfully. Enable GPS to continue."
     * And also runs attemptEnableGPS();
     */
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

    /**
     * Removes the LocationListener and stops updating location
     */
    public void stopUpdatingLocation(){
        locationManager.removeUpdates(locationListener);
    }

    /**
     * Gets the current best location
     * @return the current best location
     */
    public Location getLocation(){
        return currentBestLoc;
    }

    /**
     * Attempts to enable GPS by bringing user to GPS settings
     */
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

    /**
     * Restarts location finding
     */
    public void restart(){
        stopUpdatingLocation();
        startUpdatingLocation();
    }

    /**
     * Tests if GPS is enabled on the users device
     * @return boolean if GPS signal is available
     */
    public boolean isGPSEnabled(){
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
