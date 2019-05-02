package com.ottawa.spootr2.location;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.ottawa.spootr2.R;
import com.ottawa.spootr2.activity.LocationActivity;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class MyLocationService extends Service {

    public static final String BROADCAST_LOCATION_UPDATED = "LocationUpdated";
    private Location currentLocation;
    private Thread periodicUpdateThread;
    private Date lastBroadcastTime;

    public MyLocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initLocationListener();
        return START_NOT_STICKY;
    }

    private void initLocationListener() {

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.v("GPS Error: ", e.getMessage());
        }

        if (!gps_enabled) {
            buildAlertMessageNoGPS();
            return;
        }

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                sendLocationUpdateNotification(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION};
            if (LocationActivity.sharedActivity != null)
                ActivityCompat.requestPermissions(LocationActivity.sharedActivity, permissions, 1);
            else {
                Toast.makeText(this, "Please lauch Spootr App to grant location permissions.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);


        if (periodicUpdateThread != null) {
            periodicUpdateThread.interrupt();
        }

        lastBroadcastTime = new Date();

        periodicUpdateThread =
                (new Thread(new Runnable() {
                    @Override
                    public void run() {

                        for (; ; ) {
                            try {
                                Thread.sleep(1 * 1000, 0);

                                long timeAfterLastUpdate = TimeUnit.MILLISECONDS.toSeconds((new Date()).getTime() -
                                        lastBroadcastTime.getTime());
                                if (timeAfterLastUpdate > 15)
                                    sendLocationUpdateNotification(currentLocation);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }));

        periodicUpdateThread.start();
    }

    private void buildAlertMessageNoGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.sharedActivity);
        builder.setMessage(R.string.location_alert)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        LocationActivity.sharedActivity.startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
    }

    private void sendLocationUpdateNotification(Location location) {

        synchronized (this) {
            if (location == null) return;
            currentLocation = location;

            lastBroadcastTime = new Date();
            MyBroadcastManager.getInstance(this).sendBroadcast(BROADCAST_LOCATION_UPDATED, currentLocation);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        periodicUpdateThread.interrupt();
        periodicUpdateThread = null;
    }
}
